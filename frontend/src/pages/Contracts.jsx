import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, CircularProgress, Typography
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import dayjs from 'dayjs';
import PageHeader from '../components/PageHeader';
import StatusChip from '../components/StatusChip';
import { useContracts, useCreateContract } from '../api/hooks';
import { COLORS } from '../theme/theme';

export default function Contracts() {
  const navigate = useNavigate();
  const { data: contracts, isLoading } = useContracts();
  const createMutation = useCreateContract();
  const [open, setOpen] = useState(false);
  const [formData, setFormData] = useState({ name: '', description: '', effectiveDate: '' });

  const handleOpen = () => setOpen(true);
  const handleClose = () => {
    setOpen(false);
    setFormData({ name: '', description: '', effectiveDate: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await createMutation.mutateAsync({
      ...formData,
      effectiveDate: dayjs(formData.effectiveDate).toISOString(),
    });
    handleClose();
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <PageHeader
        title="Contracts"
        subtitle="Manage temporal agreements and view current states"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpen}>
            Create Contract
          </Button>
        }
      />

      <TableContainer component={Paper} sx={{ bgcolor: COLORS.bg.card, borderRadius: 2 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Rules</TableCell>
              <TableCell>Effective Date</TableCell>
              <TableCell>Created</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {contracts?.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                  <Typography color="text.secondary">No contracts found</Typography>
                </TableCell>
              </TableRow>
            ) : (
              contracts?.map((row) => (
                <TableRow
                  key={row.id}
                  hover
                  onClick={() => navigate(`/contracts/${row.id}`)}
                  sx={{ cursor: 'pointer', '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell sx={{ fontWeight: 600 }}>{row.name}</TableCell>
                  <TableCell><StatusChip status={row.status} /></TableCell>
                  <TableCell align="right">{row.rules?.length || 0}</TableCell>
                  <TableCell>{dayjs(row.effectiveDate).format('YYYY-MM-DD HH:mm')}</TableCell>
                  <TableCell>{dayjs(row.creationDate).format('YYYY-MM-DD HH:mm')}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <form onSubmit={handleSubmit}>
          <DialogTitle>Create New Contract</DialogTitle>
          <DialogContent>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
              <TextField
                label="Contract Name"
                required
                fullWidth
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
              <TextField
                label="Description"
                fullWidth
                multiline
                rows={3}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
              <TextField
                label="Effective Date"
                type="datetime-local"
                required
                fullWidth
                InputLabelProps={{ shrink: true }}
                value={formData.effectiveDate}
                onChange={(e) => setFormData({ ...formData, effectiveDate: e.target.value })}
              />
            </Box>
          </DialogContent>
          <DialogActions sx={{ p: 2, pt: 0 }}>
            <Button onClick={handleClose} color="inherit">Cancel</Button>
            <Button type="submit" variant="contained" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Creating...' : 'Create'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
