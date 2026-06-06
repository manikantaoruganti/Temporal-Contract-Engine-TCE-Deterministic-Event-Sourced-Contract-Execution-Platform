import { Box, Grid, Typography, CircularProgress } from '@mui/material';
import { BarChart as BarChartIcon } from '@mui/icons-material';
import PageHeader from '../components/PageHeader';
import GlowCard from '../components/GlowCard';
import { usePrometheus } from '../api/hooks';
import { COLORS } from '../theme/theme';

export default function MetricsCenter() {
  const { data: promText, isLoading, isError } = usePrometheus();

  if (isLoading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;

  // Simple parser to extract key metrics
  const parseMetric = (name) => {
    if (!promText) return 'N/A';
    const lines = promText.split('\n');
    const match = lines.find(l => l.startsWith(name));
    return match ? match.split(' ').pop() : '0';
  };

  const metrics = {
    httpReqs: parseMetric('http_server_requests_seconds_count'),
    jvmMem: (parseInt(parseMetric('jvm_memory_used_bytes')) / 1024 / 1024).toFixed(1) + ' MB',
    systemCpu: parseFloat(parseMetric('system_cpu_usage')).toFixed(3),
    jdbcActive: parseMetric('hikaricp_connections_active'),
  };

  return (
    <Box>
      <PageHeader title="Metrics Center" subtitle="Prometheus telemetry and application performance" icon={<BarChartIcon fontSize="large" />} />

      {isError ? (
        <Typography color="error">Failed to load metrics from Prometheus endpoint.</Typography>
      ) : (
        <>
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} sm={6} md={3}>
              <GlowCard title="HTTP Requests" value={metrics.httpReqs} color={COLORS.primary} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <GlowCard title="JVM Memory Used" value={metrics.jvmMem} color={COLORS.secondary} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <GlowCard title="System CPU" value={metrics.systemCpu} color={COLORS.info} />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <GlowCard title="Active JDBC Conns" value={metrics.jdbcActive} color={COLORS.warning} />
            </Grid>
          </Grid>

          <GlowCard title="Raw Prometheus Data (Scrape Endpoint)" color={COLORS.text.muted}>
            <Box sx={{ mt: 2, height: 400, overflow: 'auto', p: 2, bgcolor: '#1e1e1e', borderRadius: 1, border: `1px solid ${COLORS.border}` }}>
              <Typography variant="body2" sx={{ fontFamily: 'monospace', whiteSpace: 'pre-wrap', color: '#ccc' }}>
                {promText || 'No metrics available'}
              </Typography>
            </Box>
          </GlowCard>
        </>
      )}
    </Box>
  );
}
