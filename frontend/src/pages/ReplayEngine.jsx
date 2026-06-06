import { useState } from 'react';
import { Box, Grid, Typography, Autocomplete, TextField, Button, CircularProgress, Divider } from '@mui/material';
import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircleOutline, CancelOutlined, Shield } from '@mui/icons-material';
import PageHeader from '../components/PageHeader';
import GlowCard from '../components/GlowCard';
import StatusChip from '../components/StatusChip';
import { useContracts, useReplayContract } from '../api/hooks';
import { COLORS } from '../theme/theme';

export default function ReplayEngine() {
  const { data: contracts } = useContracts();
  const [selectedId, setSelectedId] = useState(null);
  const replayMutation = useReplayContract();
  const [report, setReport] = useState(null);

  const contractOptions = contracts?.map(c => ({ label: `${c.name} (${c.id.split('-')[0]}...)`, id: c.id })) || [];
  const liveState = contracts?.find(c => c.id === selectedId);

  const handleRunReplay = async () => {
    if (!selectedId) return;
    setReport(null);
    const res = await replayMutation.mutateAsync(selectedId);
    setReport(res);
  };

  return (
    <Box sx={{ minHeight: 'calc(100vh - 120px)' }}>
      <PageHeader 
        title="Event Sourcing Replay Engine" 
        subtitle="Cryptographically rebuild aggregate state exclusively from the event stream" 
        icon={<Shield fontSize="large" />} 
      />

      <Box sx={{ display: 'flex', gap: 2, mb: 4, alignItems: 'center' }}>
        <Autocomplete
          options={contractOptions}
          sx={{ width: 400 }}
          renderInput={(params) => <TextField {...params} label="Target Contract for Audit" />}
          onChange={(_, val) => { setSelectedId(val?.id || null); setReport(null); }}
        />
        <Button 
          variant="contained" 
          color="secondary" 
          size="large" 
          onClick={handleRunReplay}
          disabled={!selectedId || replayMutation.isPending}
          sx={{ height: 56, px: 4, fontWeight: 'bold' }}
        >
          {replayMutation.isPending ? <CircularProgress size={24} color="inherit" /> : 'RUN REPLAY AUDIT'}
        </Button>
      </Box>

      <AnimatePresence>
        {report && (
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
            {/* HERO VERDICT */}
            <Box sx={{ 
              display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 6, p: 4, 
              bgcolor: report.isConsistent ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)',
              border: `2px solid ${report.isConsistent ? COLORS.success : COLORS.error}`,
              borderRadius: 3, boxShadow: `0 0 40px ${report.isConsistent ? 'rgba(16,185,129,0.2)' : 'rgba(239,68,68,0.2)'}`
            }}>
              <motion.div initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ type: 'spring', damping: 10 }}>
                {report.isConsistent ? 
                  <CheckCircleOutline sx={{ fontSize: 80, color: COLORS.success, mb: 2 }} /> : 
                  <CancelOutlined sx={{ fontSize: 80, color: COLORS.error, mb: 2 }} />
                }
              </motion.div>
              <Typography variant="h2" sx={{ fontWeight: 900, color: report.isConsistent ? COLORS.success : COLORS.error, letterSpacing: '0.1em' }}>
                {report.isConsistent ? 'CONSISTENT' : 'INCONSISTENT'}
              </Typography>
              <Typography variant="subtitle1" sx={{ mt: 1, color: 'text.secondary' }}>
                {report.isConsistent ? 'Rebuilt state perfectly matches primary database state.' : 'State drift detected! Event stream does not match database.'}
              </Typography>
            </Box>

            {/* SIDE BY SIDE */}
            <Grid container spacing={4} sx={{ mb: 4 }}>
              <Grid item xs={12} md={6}>
                <GlowCard title="Primary Database State" color={COLORS.primary}>
                  <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box><Typography variant="caption" color="text.secondary">Name</Typography><Typography>{liveState?.name}</Typography></Box>
                    <Box><Typography variant="caption" color="text.secondary">Status</Typography><Box mt={0.5}><StatusChip status={liveState?.status} /></Box></Box>
                    <Box><Typography variant="caption" color="text.secondary">Retry Count</Typography><Typography>{liveState?.retryCount || 0}</Typography></Box>
                    <Box><Typography variant="caption" color="text.secondary">Rules Count</Typography><Typography>{liveState?.rules?.length || 0}</Typography></Box>
                  </Box>
                </GlowCard>
              </Grid>
              <Grid item xs={12} md={6}>
                <GlowCard title="Rebuilt Projection State" color={COLORS.secondary}>
                  <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box><Typography variant="caption" color="text.secondary">Name</Typography><Typography>{report.rebuiltState.name}</Typography></Box>
                    <Box><Typography variant="caption" color="text.secondary">Status</Typography><Box mt={0.5}><StatusChip status={report.rebuiltState.status} /></Box></Box>
                    <Box><Typography variant="caption" color="text.secondary">Retry Count</Typography><Typography>{report.rebuiltState.retryCount}</Typography></Box>
                    <Box><Typography variant="caption" color="text.secondary">Rules Count</Typography><Typography>{report.rebuiltState.rulesCount}</Typography></Box>
                  </Box>
                </GlowCard>
              </Grid>
            </Grid>

            {/* DISCREPANCIES */}
            {!report.isConsistent && (
              <GlowCard title="Discrepancies Found" color={COLORS.error} sx={{ mb: 4, bgcolor: 'rgba(239,68,68,0.05)' }}>
                <Box sx={{ mt: 2 }}>
                  {report.discrepancies.map((d, i) => (
                    <Typography key={i} color="error" sx={{ fontFamily: 'monospace', mb: 1 }}>• {d}</Typography>
                  ))}
                </Box>
              </GlowCard>
            )}

            {/* EVENTS APPLIED */}
            <GlowCard title="Events Replayed" color={COLORS.info}>
              <Box sx={{ mt: 2, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {report.eventsReplayed.map((evt, idx) => (
                  <motion.div key={evt.eventId} initial={{ opacity: 0, scale: 0.8 }} animate={{ opacity: 1, scale: 1 }} transition={{ delay: idx * 0.02 }}>
                    <Box sx={{ px: 2, py: 1, bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2, border: `1px solid ${COLORS.border}` }}>
                      <Typography variant="caption" sx={{ color: COLORS.info, fontWeight: 'bold' }}>{idx + 1}. {evt.eventType}</Typography>
                    </Box>
                  </motion.div>
                ))}
              </Box>
            </GlowCard>
          </motion.div>
        )}
      </AnimatePresence>
    </Box>
  );
}
