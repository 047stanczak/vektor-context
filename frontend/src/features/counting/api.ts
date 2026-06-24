import api from "@/lib/axios";
import { CountingItem } from "@/types";

export interface AuditRecord {
  id?: number;
  auditedLabel: string;
  auditType: string;
  auditedAt: string;
  createdAt?: string;
}

export async function fetchAudits(): Promise<AuditRecord[]> {
  const res = await api.get<AuditRecord[]>("/audit");
  return res.data;
}

export async function saveAudit(dto: AuditRecord): Promise<AuditRecord> {
  const res = await api.post<AuditRecord>("/audit", dto);
  return res.data;
}

export async function fetchBrandByProduct(
  productCode: number,
): Promise<string> {
  const res = await api.get<string>("/counting/brand-by-product", {
    params: { productCode },
  });
  return res.data;
}

export async function fetchBrands(): Promise<string[]> {
  const res = await api.get<string[]>("/counting/brands");
  return res.data;
}

export async function fetchDepartments(): Promise<string[]> {
  const res = await api.get<string[]>("/counting/departments");
  return res.data;
}

export async function fetchByBrand(brand: string): Promise<CountingItem[]> {
  const res = await api.get<CountingItem[]>("/counting/by-brand", {
    params: { brand },
  });
  return res.data;
}

export async function fetchByDepartment(
  department: string,
): Promise<CountingItem[]> {
  const res = await api.get<CountingItem[]>("/counting/by-department", {
    params: { department },
  });
  return res.data;
}

export async function fetchByProduct(
  productCode: number,
): Promise<CountingItem[]> {
  const res = await api.get<CountingItem[]>("/counting/by-product", {
    params: { productCode },
  });
  return res.data;
}

export async function searchCountingProduct(
  q: string,
): Promise<CountingItem | null> {
  const res = await api.get<CountingItem[]>("/counting/search", {
    params: { q },
  });
  return res.data[0] ?? null;
}

export async function generatePdf(payload: {
  auditedLabel: string;
  auditType: string;
  auditedAt: string;
  items: CountingItem[];
}): Promise<void> {
  const res = await api.post("/counting/report/pdf", payload, {
    responseType: "blob",
  });
  const url = URL.createObjectURL(res.data);
  window.open(url, "_blank");
}
