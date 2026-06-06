import { useState } from 'react';
import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Collapse, IconButton } from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowUp, ErrorOutline } from '@mui/icons-material';
import { Link } from 'react-router-dom';
import dayjs from 'dayjs';
import { motion } from 'framer-motion';
import PageHeader from '../components/PageHeader';
import { useDeadLetters } from '../api/hooks';
import { COLORS } from '../theme/theme';

function DLQRow({ dlq, idx }) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <motion.tr initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: idx * 0.05 }} component={TableRow} sx={{ '& > *': { borderBottom: 'unset' }, bgcolor: 'rgba(239,68,68,0.02)' }}>
        <TableCell>
          <IconButton size="small" onClick={() => setOpen(!open)} sx={{ color: COLORS.error }}>
            {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
          </IconButton>
        </TableCell>
        <TableCell>
          <Link style={{ color: COLORS.primary, textDecoration: 'none', fontWeight: 'bold' }} to={`/contracts/${dlq.contractId}`}>
            {dlq.contractId.split('-')[0]}...
          </Link>
        </TableCell>
        <TableCell>{dayjs(dlq.failedAt).format('YYYY-MM-DD HH:mm:ss')}</TableCell>
        <TableCell sx={{ color: COLORS.error, fontWeight: 500 }}>{dlq.errorMessage}</TableCell>
      </motion.tr>
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box sx={{ margin: 2, p: 2, bgcolor: '#1e1e1e', borderRadius: 1, border: `1px solid ${COLORS.error}44` }}>
              <Typography variant="body2" sx={{ fontFamily: 'monospace', whiteSpace: 'pre-wrap', color: '#ff9999' }}>
                {dlq.stackTrace}
              </Typography>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
}

export default function DeadLetters() {
  const { data: deadLetters } = useDeadLetters();

  return (
    <Box>
      <PageHeader title="Dead Letter Queue" subtitle="Permanent failure isolation and recovery" icon={<ErrorOutline fontSize="large" color="error" />} />

      <TableContainer component={Paper} sx={{ bgcolor: COLORS.bg.card, border: `1px solid ${COLORS.error}44` }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell width={50} />
              <TableCell>Contract ID</TableCell>
              <TableCell>Failed At</TableCell>
              <TableCell>Error Message</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {!deadLetters || deadLetters.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 8 }}>
                  <CheckCircleOutline sx={{ fontSize: 64, color: COLORS.success, mb: 2, opacity: 0.5 }} />
                  <Typography color="text.secondary" variant="h6">No dead letters — all executions healthy</Typography>
                </TableCell>
              </TableRow>
            ) : (
              deadLetters.map((dlq, idx) => <DLQRow key={dlq.id} dlq={dlq} idx={idx} />)
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
import { CheckCircleOutline } from '@mui/icons-material';
