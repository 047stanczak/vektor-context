import api from "@/lib/axios";
import { SeparationProduct } from "@/types";

export async function fetchPendingByBarcode(
  barcode: string,
): Promise<SeparationProduct[]> {
  const res = await api.get<SeparationProduct[]>("/pending-by-barcode", {
    params: { barcode },
  });
  return res.data;
}

export async function fetchPendingByCode(
  code: string,
): Promise<SeparationProduct[]> {
  const res = await api.get<SeparationProduct[]>("/pending-by-code", {
    params: { code },
  });
  return res.data;
}
