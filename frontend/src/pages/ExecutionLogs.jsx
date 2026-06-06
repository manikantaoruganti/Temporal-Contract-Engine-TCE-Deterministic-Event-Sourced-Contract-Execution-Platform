import { useState } from 'react';
import { Box, Typography, Autocomplete, TextField, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Collapse, IconButton } from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowUp } from '@mui/icons-material';
import dayjs from 'dayjs';
import PageHeader from '../components/PageHeader';
import StatusChip from '../components/StatusChip';
import { useContracts, useExecutionLogs } from '../api/hooks';
import { COLORS } from '../theme/theme';

function LogRow({ log }) {
  const [open, setOpen] = useState(false);
  const statusToMap = log.status === 'SUCCESS' ? 'COMPLETED' : log.status === 'FAILED' ? 'FAILED' : 'PENDING';

  return (
    <>
      <TableRow hover sx={{ '& > *': { borderBottom: 'unset' } }}>
        <TableCell>
          <IconButton size="small" onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
          </IconButton>
        </TableCell>
        <TableCell>{dayjs(log.executionTimestamp).format('YYYY-MM-DD HH:mm:ss.SSS')}</TableCell>
        <TableCell><StatusChip status={statusToMap} size="small" /></TableCell>
        <TableCell sx={{ maxWidth: 400, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {log.details.split('\n')[0]}
        </TableCell>
      </TableRow>
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box sx={{ margin: 2, p: 2, bgcolor: '#1e1e1e', borderRadius: 1, border: `1px solid ${COLORS.border}` }}>
              <Typography variant="body2" sx={{ fontFamily: 'monospace', whiteSpace: 'pre-wrap', color: '#ccc' }}>
                {log.details}
              </Typography>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
}

export default function ExecutionLogs() {
  const { data: contracts } = useContracts();
  const [selectedId, setSelectedId] = useState(null);
  const { data: logs } = useExecutionLogs(selectedId);

  const contractOptions = contracts?.map(c => ({ label: `${c.name} (${c.id.split('-')[0]}...)`, id: c.id })) || [];

  return (
    <Box>
      <PageHeader title="Execution Logs" subtitle="Detailed breakdown of rule evaluation and execution outcomes" />

      <Box sx={{ mb: 4 }}>
        <Autocomplete
          options={contractOptions}
          sx={{ width: 400 }}
          renderInput={(params) => <TextField {...params} label="Select Contract" />}
          onChange={(_, val) => setSelectedId(val?.id || null)}
        />
      </Box>

      {!selectedId ? (
        <Box sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">Select a contract to view its execution history.</Typography>
        </Box>
      ) : (
        <TableContainer component={Paper} sx={{ bgcolor: COLORS.bg.card }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell width={50} />
                <TableCell>Timestamp</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Details Preview</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs?.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} align="center" sx={{ py: 4 }}>No execution logs found</TableCell>
                </TableRow>
              ) : (
                logs?.map((log) => <LogRow key={log.id} log={log} />)
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
