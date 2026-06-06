import { alpha } from '@mui/material/styles';
import { Chip } from '@mui/material';

const STATUS_MAP = {
  PENDING: { color: '#F59E0B', bg: 'rgba(245,158,11,0.12)', label: 'Pending' },
  ELIGIBLE: { color: '#3B82F6', bg: 'rgba(59,130,246,0.12)', label: 'Eligible' },
  EXECUTING: { color: '#8B5CF6', bg: 'rgba(139,92,246,0.12)', label: 'Executing' },
  COMPLETED: { color: '#10B981', bg: 'rgba(16,185,129,0.12)', label: 'Completed' },
  FAILED: { color: '#EF4444', bg: 'rgba(239,68,68,0.12)', label: 'Failed' },
  CANCELLED: { color: '#64748B', bg: 'rgba(100,116,139,0.12)', label: 'Cancelled' },
};

export default function StatusChip({ status, size = 'small', ...props }) {
  const cfg = STATUS_MAP[status] || STATUS_MAP.PENDING;
  return (
    <Chip
      label={cfg.label}
      size={size}
      sx={{
        color: cfg.color,
        backgroundColor: cfg.bg,
        border: `1px solid ${alpha(cfg.color, 0.3)}`,
        fontWeight: 700,
        letterSpacing: '0.04em',
        ...props.sx,
      }}
      {...props}
    />
  );
}

export { STATUS_MAP };
