import api from "@/lib/axios";
import { ApiResponse } from "@/types";

export interface Task {
  id: number;
  title: string;
  status: "TODO" | "DONE";
  priority: "LOW" | "MEDIUM" | "HIGH";
  energyLevel: "QUICK" | "HEAVY" | "BORING" | "FOCUS";
  frequency: "ONCE" | "WEEKLY_1X" | "WEEKLY_2X" | "WEEKLY_3X";
  tags: string[];
  weeklyTarget: number;
  weeklyDone: number;
}

export interface TaskRequest {
  title: string;
  priority: string;
  energyLevel: string;
  frequency: string;
  tags: string[];
}

export async function fetchTasks(): Promise<Task[]> {
  const res = await api.get<ApiResponse<Task[]>>("/tasks");
  return res.data.data ?? [];
}

export async function createTask(req: TaskRequest): Promise<Task> {
  const res = await api.post<ApiResponse<Task>>("/tasks", req);
  return res.data.data!;
}

export async function updateTask(
  id: number,
  req: Partial<TaskRequest>,
): Promise<Task> {
  const res = await api.put<ApiResponse<Task>>(`/tasks/${id}`, req);
  return res.data.data!;
}

export async function completeTask(id: number): Promise<Task> {
  const res = await api.post<ApiResponse<Task>>(`/tasks/${id}/complete`);
  return res.data.data!;
}

export async function deleteTask(id: number): Promise<void> {
  await api.delete(`/tasks/${id}`);
}
