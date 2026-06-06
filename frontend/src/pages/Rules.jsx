import { useState } from 'react';
import { Box, Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Autocomplete, Grid, Chip } from '@mui/material';
import { Add, Rule as RuleIcon } from '@mui/icons-material';
import PageHeader from '../components/PageHeader';
import GlowCard from '../components/GlowCard';
import { useContracts, useCreateRule } from '../api/hooks';
import { COLORS } from '../theme/theme';

export default function Rules() {
  const { data: contracts } = useContracts();
  const createMutation = useCreateRule();
  const [open, setOpen] = useState(false);
  const [formData, setFormData] = useState({ contractId: null, name: '', description: '', conditionExpression: '', actionPayload: '', priority: 1 });

  const contractOptions = contracts?.map(c => ({ label: c.name, id: c.id })) || [];

  const handleSubmit = async (e) => {
    e.preventDefault();
    await createMutation.mutateAsync({ ...formData, contractId: formData.contractId.id });
    setOpen(false);
    setFormData({ contractId: null, name: '', description: '', conditionExpression: '', actionPayload: '', priority: 1 });
  };

  return (
    <Box>
      <PageHeader 
        title="Rules Engine" 
        subtitle="Manage logical conditions and actions for contracts" 
        action={<Button variant="contained" startIcon={<Add />} onClick={() => setOpen(true)}>Create Rule</Button>}
      />

      {contracts?.map(contract => (
        contract.rules && contract.rules.length > 0 && (
          <Box key={contract.id} sx={{ mb: 6 }}>
            <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
              <RuleIcon color="primary" /> {contract.name} <Typography variant="caption" color="text.secondary">({contract.id})</Typography>
            </Typography>
            <Grid container spacing={3}>
              {contract.rules.map(rule => (
                <Grid item xs={12} md={6} lg={4} key={rule.id}>
                  <GlowCard title={rule.name} subtitle={rule.description} color={COLORS.info}>
                    <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Chip label={`Priority: ${rule.priority}`} size="small" sx={{ bgcolor: 'rgba(59,130,246,0.1)', color: COLORS.info }} />
                    </Box>
                    <Box sx={{ mt: 2, p: 1.5, bgcolor: '#1e1e1e', borderRadius: 1, border: `1px solid ${COLORS.border}` }}>
                      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>Condition (SpEL)</Typography>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace', color: COLORS.secondary }}>{rule.conditionExpression}</Typography>
                    </Box>
                    <Box sx={{ mt: 1, p: 1.5, bgcolor: '#1e1e1e', borderRadius: 1, border: `1px solid ${COLORS.border}` }}>
                      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>Action Payload</Typography>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace', color: '#ccc' }}>{rule.actionPayload}</Typography>
                    </Box>
                  </GlowCard>
                </Grid>
              ))}
            </Grid>
          </Box>
        )
      ))}

      {contracts?.every(c => !c.rules || c.rules.length === 0) && (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography color="text.secondary" variant="h6">No rules found in any contract. Create one to get started.</Typography>
        </Box>
      )}

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <form onSubmit={handleSubmit}>
          <DialogTitle>Create New Rule</DialogTitle>
          <DialogContent>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
              <Autocomplete
                options={contractOptions}
                value={formData.contractId}
                onChange={(_, val) => setFormData({ ...formData, contractId: val })}
                renderInput={(params) => <TextField {...params} label="Target Contract" required />}
              />
              <TextField label="Rule Name" required value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} />
              <TextField label="Description" value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} />
              <TextField label="Condition Expression (SpEL)" required value={formData.conditionExpression} onChange={(e) => setFormData({ ...formData, conditionExpression: e.target.value })} placeholder="e.g. balance > 5000" />
              <TextField label="Action Payload (JSON)" required value={formData.actionPayload} onChange={(e) => setFormData({ ...formData, actionPayload: e.target.value })} placeholder='{"action": "APPROVE"}' />
              <TextField label="Priority" type="number" required value={formData.priority} onChange={(e) => setFormData({ ...formData, priority: parseInt(e.target.value) })} />
            </Box>
          </DialogContent>
          <DialogActions sx={{ p: 2 }}>
            <Button onClick={() => setOpen(false)} color="inherit">Cancel</Button>
            <Button type="submit" variant="contained" color="primary" disabled={createMutation.isPending || !formData.contractId}>Create Rule</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
}
