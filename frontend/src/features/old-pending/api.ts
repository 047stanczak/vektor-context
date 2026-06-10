import api from "@/lib/axios";
import { SeparationProduct } from "@/types";

export async function fetchOldPending(
  days: number,
): Promise<SeparationProduct[]> {
  const res = await api.get<SeparationProduct[]>("/old-pending", {
    params: { days },
  });
  return res.data;
}

export async function fetchOldPendingWithStock(
  days: number,
): Promise<SeparationProduct[]> {
  const res = await api.get<SeparationProduct[]>("/old-pending-with-stock", {
    params: { days },
  });
  return res.data;
}

export async function fetchOldPendingNoStock(
  days: number,
): Promise<SeparationProduct[]> {
  const res = await api.get<SeparationProduct[]>("/old-pending-no-stock", {
    params: { days },
  });
  return res.data;
}
