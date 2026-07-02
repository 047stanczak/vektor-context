import { describe, it, expect, beforeEach } from "vitest";
import {
  detectInputMode,
  formatDate,
  formatNum,
  formatDdMmYyyyInput,
  isValidDdMmYyyy,
  tipoBadgeClass,
  getRecentSeparators,
  addRecentSeparator,
} from "./utils";

describe("detectInputMode", () => {
  it("detecta barcode quando tem letra", () => {
    expect(detectInputMode("ABC123")).toBe("barcode");
  });
  it("detecta barcode quando tem 8+ dígitos", () => {
    expect(detectInputMode("12345678")).toBe("barcode");
  });
  it("detecta productCode quando é curto e numérico", () => {
    expect(detectInputMode("1234")).toBe("productCode");
  });
});

describe("formatDate", () => {
  it("formata ISO para pt-BR", () => {
    expect(formatDate("2026-07-02")).toBe("02/07/2026");
  });
});

describe("formatNum", () => {
  it("retorna traço para null/undefined", () => {
    expect(formatNum(null)).toBe("—");
    expect(formatNum(undefined)).toBe("—");
  });
  it("mantém inteiros sem casas decimais", () => {
    expect(formatNum(10)).toBe("10");
  });
  it("formata decimais com vírgula", () => {
    expect(formatNum(10.5)).toBe("10,50");
  });
});

describe("formatDdMmYyyyInput", () => {
  it("insere barras progressivamente", () => {
    expect(formatDdMmYyyyInput("02072026")).toBe("02/07/2026");
    expect(formatDdMmYyyyInput("0207")).toBe("02/07");
    expect(formatDdMmYyyyInput("02")).toBe("02");
  });
  it("ignora caracteres não numéricos", () => {
    expect(formatDdMmYyyyInput("02a07b2026")).toBe("02/07/2026");
  });
});

describe("isValidDdMmYyyy", () => {
  it("aceita data válida", () => {
    expect(isValidDdMmYyyy("02/07/2026")).toBe(true);
  });
  it("rejeita formato inválido", () => {
    expect(isValidDdMmYyyy("2026-07-02")).toBe(false);
  });
  it("rejeita data inexistente", () => {
    expect(isValidDdMmYyyy("31/02/2026")).toBe(false);
  });
  it("rejeita mês inválido", () => {
    expect(isValidDdMmYyyy("01/13/2026")).toBe(false);
  });
});

describe("tipoBadgeClass", () => {
  it("retorna classe para FALTA", () => {
    expect(tipoBadgeClass("FALTA")).toContain("red");
  });
  it("retorna classe para SOBRA", () => {
    expect(tipoBadgeClass("SOBRA")).toContain("green");
  });
  it("retorna classe padrão para tipo desconhecido", () => {
    expect(tipoBadgeClass("OUTRO")).toContain("gray");
  });
});

describe("getRecentSeparators / addRecentSeparator", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("retorna lista vazia quando não há nada salvo", () => {
    expect(getRecentSeparators()).toEqual([]);
  });

  it("adiciona e recupera separador", () => {
    addRecentSeparator("João");
    expect(getRecentSeparators()).toEqual(["João"]);
  });

  it("move separador existente para o topo sem duplicar", () => {
    addRecentSeparator("João");
    addRecentSeparator("Maria");
    addRecentSeparator("João");
    expect(getRecentSeparators()).toEqual(["João", "Maria"]);
  });
});
