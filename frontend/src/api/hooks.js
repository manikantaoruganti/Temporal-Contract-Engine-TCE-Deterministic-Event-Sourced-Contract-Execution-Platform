import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as api from './client';

// ─── Contracts ───────────────────────────────────────────────────────────────
export const useContracts = (refetchInterval) =>
  useQuery({ queryKey: ['contracts'], queryFn: api.fetchContracts, refetchInterval: refetchInterval || false });

export const useContract = (id) =>
  useQuery({ queryKey: ['contract', id], queryFn: () => api.fetchContract(id), enabled: !!id });

export const useCreateContract = () => {
  const qc = useQueryClient();
  return useMutation({ mutationFn: api.createContract, onSuccess: () => qc.invalidateQueries({ queryKey: ['contracts'] }) });
};

export const useExecuteContract = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, facts }) => api.executeContract(id, facts),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['contracts'] });
      qc.invalidateQueries({ queryKey: ['contract', vars.id] });
    },
  });
};

export const useCancelContract = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => api.cancelContract(id),
    onSuccess: (_, id) => {
      qc.invalidateQueries({ queryKey: ['contracts'] });
      qc.invalidateQueries({ queryKey: ['contract', id] });
    },
  });
};

// ─── Events & Logs ───────────────────────────────────────────────────────────
export const useContractEvents = (id) =>
  useQuery({ queryKey: ['events', id], queryFn: () => api.fetchContractEvents(id), enabled: !!id });

export const useExecutionLogs = (id) =>
  useQuery({ queryKey: ['execution-logs', id], queryFn: () => api.fetchExecutionLogs(id), enabled: !!id });

// ─── Rules ───────────────────────────────────────────────────────────────────
export const useCreateRule = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: api.createRule,
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['contracts'] });
      qc.invalidateQueries({ queryKey: ['contract', vars.contractId] });
    },
  });
};

// ─── Admin ───────────────────────────────────────────────────────────────────
export const useReplayContract = () =>
  useMutation({ mutationFn: (id) => api.replayContract(id) });

export const useDeadLetters = () =>
  useQuery({ queryKey: ['dead-letters'], queryFn: api.fetchDeadLetters });

// ─── Health ──────────────────────────────────────────────────────────────────
export const useHealth = () =>
  useQuery({ queryKey: ['health'], queryFn: api.fetchActuatorHealth, refetchInterval: 30000 });

export const usePrometheus = () =>
  useQuery({ queryKey: ['prometheus'], queryFn: api.fetchPrometheus, refetchInterval: 30000 });
