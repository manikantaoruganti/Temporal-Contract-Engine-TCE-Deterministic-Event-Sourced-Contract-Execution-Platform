import { Box, Typography } from '@mui/material';
import { AccountTree } from '@mui/icons-material';
import { ReactFlow, Background, Controls, MiniMap } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import PageHeader from '../components/PageHeader';
import { COLORS } from '../theme/theme';

const initialNodes = [
  { id: '1', position: { x: 400, y: 50 }, data: { label: 'Frontend (React)' }, style: { background: '#06B6D4', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '2', position: { x: 400, y: 150 }, data: { label: 'REST API Gateway' }, style: { background: '#10B981', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '3', position: { x: 400, y: 250 }, data: { label: 'Contract Service' }, style: { background: '#6366F1', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '4', position: { x: 150, y: 350 }, data: { label: 'Rule Engine' }, style: { background: '#8B5CF6', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '5', position: { x: 650, y: 350 }, data: { label: 'Replay Engine' }, style: { background: '#F59E0B', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '6', position: { x: 400, y: 450 }, data: { label: 'Event Store Service' }, style: { background: '#3B82F6', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '7', position: { x: 250, y: 600 }, data: { label: 'PostgreSQL' }, style: { background: '#336791', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '8', position: { x: 550, y: 600 }, data: { label: 'Kafka' }, style: { background: '#E2533A', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
  { id: '9', position: { x: 100, y: 250 }, data: { label: 'Redis Cache' }, style: { background: '#DC382D', color: '#fff', border: 'none', borderRadius: 8, padding: 10, width: 200, textAlign: 'center', fontWeight: 'bold' } },
];

const initialEdges = [
  { id: 'e1-2', source: '1', target: '2', animated: true, style: { stroke: COLORS.primary, strokeWidth: 2 } },
  { id: 'e2-3', source: '2', target: '3', animated: true, style: { stroke: COLORS.primary, strokeWidth: 2 } },
  { id: 'e3-4', source: '3', target: '4', animated: true, style: { stroke: COLORS.secondary, strokeWidth: 2 } },
  { id: 'e3-6', source: '3', target: '6', animated: true, style: { stroke: COLORS.info, strokeWidth: 2 } },
  { id: 'e6-7', source: '6', target: '7', animated: true, style: { stroke: COLORS.text.secondary, strokeWidth: 2 } },
  { id: 'e6-8', source: '6', target: '8', animated: true, style: { stroke: COLORS.warning, strokeWidth: 2 } },
  { id: 'e3-9', source: '3', target: '9', animated: true, style: { stroke: COLORS.error, strokeWidth: 2 } },
  { id: 'e8-5', source: '8', target: '5', animated: true, style: { stroke: COLORS.warning, strokeWidth: 2 } },
];

export default function Architecture() {
  return (
    <Box sx={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="System Architecture" subtitle="Interactive visualization of the Event Sourced backend" icon={<AccountTree fontSize="large" />} />
      <Box sx={{ flexGrow: 1, borderRadius: 2, overflow: 'hidden', border: `1px solid ${COLORS.border}`, bgcolor: '#111' }}>
        <ReactFlow nodes={initialNodes} edges={initialEdges} fitView>
          <Background color={COLORS.text.muted} gap={16} />
          <Controls />
          <MiniMap nodeColor={(n) => n.style?.background || '#fff'} />
        </ReactFlow>
      </Box>
    </Box>
  );
}
