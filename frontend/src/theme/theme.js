import { createTheme, alpha } from '@mui/material/styles';

const COLORS = {
  primary: '#6366F1',
  primaryLight: '#818CF8',
  primaryDark: '#4F46E5',
  secondary: '#06B6D4',
  secondaryLight: '#22D3EE',
  success: '#10B981',
  warning: '#F59E0B',
  error: '#EF4444',
  info: '#3B82F6',
  bg: {
    main: '#0B0F1A',
    paper: '#111827',
    card: '#1A1F2E',
    elevated: '#232838',
    glass: 'rgba(17, 24, 39, 0.7)',
  },
  text: {
    primary: '#F1F5F9',
    secondary: '#94A3B8',
    muted: '#64748B',
  },
  border: 'rgba(99, 102, 241, 0.15)',
  glow: 'rgba(99, 102, 241, 0.25)',
};

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: COLORS.primary, light: COLORS.primaryLight, dark: COLORS.primaryDark },
    secondary: { main: COLORS.secondary, light: COLORS.secondaryLight },
    success: { main: COLORS.success },
    warning: { main: COLORS.warning },
    error: { main: COLORS.error },
    info: { main: COLORS.info },
    background: { default: COLORS.bg.main, paper: COLORS.bg.paper },
    text: { primary: COLORS.text.primary, secondary: COLORS.text.secondary },
    divider: COLORS.border,
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: { fontWeight: 700, letterSpacing: '-0.02em' },
    h2: { fontWeight: 700, letterSpacing: '-0.02em' },
    h3: { fontWeight: 600, letterSpacing: '-0.01em' },
    h4: { fontWeight: 600 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    subtitle1: { color: COLORS.text.secondary, fontWeight: 500 },
    subtitle2: { color: COLORS.text.muted, fontWeight: 500 },
    body2: { color: COLORS.text.secondary },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: { borderRadius: 12 },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          background: `linear-gradient(135deg, ${COLORS.bg.main} 0%, #0F172A 50%, ${COLORS.bg.main} 100%)`,
          minHeight: '100vh',
        },
        '::-webkit-scrollbar': { width: 6 },
        '::-webkit-scrollbar-track': { background: 'transparent' },
        '::-webkit-scrollbar-thumb': { background: COLORS.text.muted, borderRadius: 3 },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: COLORS.bg.card,
          border: `1px solid ${COLORS.border}`,
          backdropFilter: 'blur(12px)',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundColor: COLORS.bg.card,
          border: `1px solid ${COLORS.border}`,
          backdropFilter: 'blur(12px)',
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          '&:hover': {
            borderColor: alpha(COLORS.primary, 0.4),
            boxShadow: `0 0 20px ${COLORS.glow}`,
          },
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8, padding: '8px 20px', fontWeight: 600 },
        contained: {
          boxShadow: `0 0 16px ${alpha(COLORS.primary, 0.3)}`,
          '&:hover': { boxShadow: `0 0 24px ${alpha(COLORS.primary, 0.5)}` },
        },
        outlined: {
          borderColor: COLORS.border,
          '&:hover': { borderColor: COLORS.primary, backgroundColor: alpha(COLORS.primary, 0.08) },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, fontSize: '0.75rem', letterSpacing: '0.03em' },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: { borderBottom: `1px solid ${COLORS.border}` },
        head: { fontWeight: 700, color: COLORS.text.secondary, textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: '0.08em' },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          backgroundColor: COLORS.bg.elevated,
          border: `1px solid ${COLORS.border}`,
          backdropFilter: 'blur(20px)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: COLORS.bg.paper,
          borderRight: `1px solid ${COLORS.border}`,
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: COLORS.bg.elevated,
          border: `1px solid ${COLORS.border}`,
          fontSize: '0.8rem',
        },
      },
    },
  },
});

export { COLORS };
export default theme;
