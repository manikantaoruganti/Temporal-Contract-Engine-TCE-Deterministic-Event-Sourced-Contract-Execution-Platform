import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, Grid, Typography, CircularProgress, Button, Stepper, Step, StepLabel,
  Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField
} from '@mui/material';
import { PlayArrow, Stop, Replay, Assignment } from '@mui/icons-material';
import dayjs from 'dayjs';
import { motion } from 'framer-motion';
import PageHeader from '../components/PageHeader';
import StatusChip, { STATUS_MAP } from '../components/StatusChip';
import GlowCard from '../components/GlowCard';
import { useContract, useExecuteContract, useCancelContract, useExecutionLogs } from '../api/hooks';
import { COLORS } from '../theme/theme';

const LIFECYCLE_STEPS = ['PENDING', 'EXECUTING', 'COMPLETED'];

export default function ContractDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: contract, isLoading } = useContract(id);
  const { data: logs } = useExecutionLogs(id);
  const executeMutation = useExecuteContract();
  const cancelMutation = useCancelContract();

  const [execDialogOpen, setExecDialogOpen] = useState(false);
  const [factsJson, setFactsJson] = useState('{\n  "balance": 5000,\n  "risk_score": 10\n}');

  if (isLoading || !contract) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
        <CircularProgress />
      </Box>
    );
  }

  const handleExecute = async () => {
    try {
      const facts = JSON.parse(factsJson);
      await executeMutation.mutateAsync({ id, facts });
      setExecDialogOpen(false);
    } catch (e) {
      alert('Invalid JSON facts or execution failed');
    }
  };

  const handleCancel = async () => {
    if (window.confirm('Are you sure you want to cancel this contract?')) {
      await cancelMutation.mutateAsync(id);
    }
  };

  const getActiveStep = () => {
    if (contract.status === 'COMPLETED') return 3;
    if (contract.status === 'EXECUTING') return 1;
    if (contract.status === 'FAILED' || contract.status === 'CANCELLED') return 1; // Show stuck in middle
    return 0; // PENDING
  };

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
      <PageHeader
        title={contract.name}
        subtitle={`ID: ${contract.id}`}
        icon={<Assignment fontSize="large" />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button
              variant="outlined"
              color="error"
              startIcon={<Stop />}
              onClick={handleCancel}
              disabled={['COMPLETED', 'FAILED', 'CANCELLED'].includes(contract.status)}
            >
              Cancel
            </Button>
            <Button
              variant="contained"
              color="secondary"
              startIcon={<PlayArrow />}
              onClick={() => setExecDialogOpen(true)}
              disabled={['COMPLETED', 'CANCELLED'].includes(contract.status)}
            >
              Execute
            </Button>
            <Button
              variant="contained"
              color="info"
              startIcon={<Replay />}
              onClick={() => navigate('/replay')}
            >
              Audit Replay
            </Button>
          </Box>
        }
      />

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={8}>
          <GlowCard title="State Machine Timeline" sx={{ mb: 3 }}>
            <Box sx={{ py: 3 }}>
              <Stepper activeStep={getActiveStep()} alternativeLabel>
                {LIFECYCLE_STEPS.map((label) => (
                  <Step key={label}>
                    <StepLabel>{label}</StepLabel>
                  </Step>
                ))}
              </Stepper>
              {['FAILED', 'CANCELLED'].includes(contract.status) && (
                <Typography align="center" color="error.main" sx={{ mt: 2, fontWeight: 'bold' }}>
                  Terminated: {contract.status}
                </Typography>
              )}
            </Box>
          </GlowCard>

          <Typography variant="h6" sx={{ mb: 2 }}>Execution Logs</Typography>
          <TableContainer component={Paper} sx={{ bgcolor: COLORS.bg.card }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Timestamp</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Details</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {logs?.map((log) => (
                  <TableRow key={log.id}>
                    <TableCell>{dayjs(log.executionTimestamp).format('YYYY-MM-DD HH:mm:ss')}</TableCell>
                    <TableCell><StatusChip status={log.status === 'SUCCESS' ? 'COMPLETED' : log.status} size="small" /></TableCell>
                    <TableCell sx={{ maxWidth: 300, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {log.details}
                    </TableCell>
                  </TableRow>
                ))}
                {!logs?.length && (
                  <TableRow><TableCell colSpan={3} align="center">No executions yet</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>

        <Grid item xs={12} md={4}>
          <GlowCard title="Metadata" sx={{ mb: 3 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">Status</Typography>
                <Box mt={0.5}><StatusChip status={contract.status} /></Box>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Effective Date</Typography>
                <Typography variant="body2">{dayjs(contract.effectiveDate).format('MMMM D, YYYY HH:mm')}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Description</Typography>
                <Typography variant="body2">{contract.description || 'No description provided'}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Version Lock</Typography>
                <Typography variant="body2">{contract.version}</Typography>
              </Box>
            </Box>
          </GlowCard>
        </Grid>
      </Grid>

      <Dialog open={execDialogOpen} onClose={() => setExecDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Execute Contract</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>Provide execution facts in JSON format:</Typography>
          <TextField
            fullWidth
            multiline
            rows={6}
            value={factsJson}
            onChange={(e) => setFactsJson(e.target.value)}
            InputProps={{ sx: { fontFamily: 'monospace', fontSize: '0.875rem' } }}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setExecDialogOpen(false)} color="inherit">Cancel</Button>
          <Button onClick={handleExecute} variant="contained" color="secondary" disabled={executeMutation.isPending}>
            {executeMutation.isPending ? 'Executing...' : 'Run Execution'}
          </Button>
        </DialogActions>
      </Dialog>
    </motion.div>
  );
}
