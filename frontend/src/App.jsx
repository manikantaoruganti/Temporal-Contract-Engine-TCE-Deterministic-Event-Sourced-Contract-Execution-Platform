import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import theme from './theme/theme';
import AppLayout from './layouts/AppLayout';
import { lazy, Suspense } from 'react';
import { Box, CircularProgress } from '@mui/material';

const Loading = () => (
  <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
    <CircularProgress sx={{ color: 'primary.main' }} />
  </Box>
);

const Dashboard = lazy(() => import('./pages/Dashboard'));
const Contracts = lazy(() => import('./pages/Contracts'));
const ContractDetail = lazy(() => import('./pages/ContractDetail'));
const Rules = lazy(() => import('./pages/Rules'));
const EventExplorer = lazy(() => import('./pages/EventExplorer'));
const ReplayEngine = lazy(() => import('./pages/ReplayEngine'));
const ExecutionLogs = lazy(() => import('./pages/ExecutionLogs'));
const DeadLetters = lazy(() => import('./pages/DeadLetters'));
const MetricsCenter = lazy(() => import('./pages/MetricsCenter'));
const SystemHealth = lazy(() => import('./pages/SystemHealth'));
const Architecture = lazy(() => import('./pages/Architecture'));
const AboutProject = lazy(() => import('./pages/AboutProject'));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 10000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <Suspense fallback={<Loading />}>
            <Routes>
              <Route element={<AppLayout />}>
                <Route path="/" element={<Dashboard />} />
                <Route path="/contracts" element={<Contracts />} />
                <Route path="/contracts/:id" element={<ContractDetail />} />
                <Route path="/rules" element={<Rules />} />
                <Route path="/events" element={<EventExplorer />} />
                <Route path="/replay" element={<ReplayEngine />} />
                <Route path="/execution-logs" element={<ExecutionLogs />} />
                <Route path="/dead-letters" element={<DeadLetters />} />
                <Route path="/metrics" element={<MetricsCenter />} />
                <Route path="/health" element={<SystemHealth />} />
                <Route path="/architecture" element={<Architecture />} />
                <Route path="/about" element={<AboutProject />} />
              </Route>
            </Routes>
          </Suspense>
        </BrowserRouter>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
