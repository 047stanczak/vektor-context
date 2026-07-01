package com.vektorcontext.services;

import com.vektorcontext.dto.DivergenceQueryResponse;
import com.vektorcontext.dto.DivergenceRecordRequest;
import com.vektorcontext.dto.DivergenceRecordResponse;
import com.vektorcontext.exception.DivergenceNotFoundException;
import com.vektorcontext.exception.ProductNotFoundException;
import com.vektorcontext.models.DivergenceRecord;
import com.vektorcontext.models.Product;
import com.vektorcontext.repository.DivergenceRecordRepository;
import com.vektorcontext.repository.ProductRepository;
import com.vektorcontext.repository.StockSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DivergenceService {

    private final DivergenceRecordRepository divergenceRecordRepository;
    private final ProductRepository productRepository;
    private final StockSnapshotRepository stockSnapshotRepository;
    private final TransactionFinder transactionFinder;

    public DivergenceService(DivergenceRecordRepository divergenceRecordRepository,
                             ProductRepository productRepository,
                             StockSnapshotRepository stockSnapshotRepository,
                             TransactionFinder transactionFinder) {
        this.divergenceRecordRepository = divergenceRecordRepository;
        this.productRepository = productRepository;
        this.stockSnapshotRepository = stockSnapshotRepository;
        this.transactionFinder = transactionFinder;
    }

    public DivergenceQueryResponse queryByProductCode(Integer productCode, Integer storeCode) {
        Product product = productRepository.findById(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado: " + productCode));
        return buildQueryResponse(product, storeCode);
    }

    public DivergenceQueryResponse queryByBarcode(String barcode, Integer storeCode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado pelo barcode: " + barcode));
        return buildQueryResponse(product, storeCode);
    }

    public List<DivergenceRecordResponse> saveAll(List<DivergenceRecordRequest> requests) {
        List<DivergenceRecord> records = requests.stream()
                .map(this::toEntity)
                .toList();

        List<DivergenceRecord> saved = divergenceRecordRepository.saveAll(records);
        return enrich(saved);
    }

    public List<DivergenceRecordResponse> findByDate(LocalDate date) {
        List<DivergenceRecord> records = divergenceRecordRepository.findByDate(date);
        return enrich(records);
    }

    public DivergenceRecord findById(Long id) {
        return divergenceRecordRepository.findById(id)
                .orElseThrow(() -> new DivergenceNotFoundException("Divergência não encontrada: " + id));
    }

    public DivergenceRecordResponse update(Long id, DivergenceRecordRequest request) {
        DivergenceRecord record = findById(id);

        record.setDate(request.getDate());
        record.setStoreCode(request.getStoreCode());
        record.setProductCode(request.getProductCode());
        record.setTipo(request.getTipo());
        record.setQuantity(request.getQuantity());
        record.setCurrentStock(request.getCurrentStock());
        record.setSeparatorName(request.getSeparatorName());
        record.setNf(request.getNf());
        record.setObservation(request.getObservation());

        DivergenceRecord saved = divergenceRecordRepository.save(record);
        return enrich(List.of(saved)).get(0);
    }

    public void delete(Long id) {
        findById(id);
        divergenceRecordRepository.deleteById(id);
    }

    public List<DivergenceRecordResponse> findByDateForReport(LocalDate date) {
        List<DivergenceRecord> records = divergenceRecordRepository.findByDate(date);
        if (records.isEmpty()) {
            throw new DivergenceNotFoundException("Nenhuma divergência encontrada para " + date);
        }
        return enrichWithStockSnapshot(records, date);
    }


    private DivergenceQueryResponse buildQueryResponse(Product product, Integer storeCode) {
        String separatorName = transactionFinder.findSeparatorName(product.getCode(), storeCode);
        Double currentStock = transactionFinder.findCurrentStock(product.getCode());

        DivergenceQueryResponse response = new DivergenceQueryResponse();
        response.setProductCode(product.getCode());
        response.setProductDescription(product.getDescription());
        response.setProductComplement(product.getComplement());
        response.setBarcode(product.getBarcode());
        response.setCurrentStock(currentStock);
        response.setSeparatorName(separatorName);
        response.setSeparationDate(transactionFinder.findSeparationDate(product.getCode(), storeCode));
        return response;
    }

    private List<DivergenceRecordResponse> enrichWithStockSnapshot(List<DivergenceRecord> records, LocalDate date) {
        List<Integer> codes = records.stream()
                .map(DivergenceRecord::getProductCode)
                .distinct()
                .toList();

        Map<Integer, Product> productMap = productRepository.findAllById(codes)
                .stream()
                .collect(Collectors.toMap(Product::getCode, p -> p));

        return records.stream()
                .map(r -> {
                    Double stockFromSnapshot = stockSnapshotRepository
                            .findByProductCodeAndDate(r.getProductCode(), date)
                            .map(s -> s.getCurrentStock())
                            .orElse(0.0);
                    return toResponse(r, productMap.get(r.getProductCode()), stockFromSnapshot);
                })
                .toList();
    }

    private List<DivergenceRecordResponse> enrich(List<DivergenceRecord> records) {
        List<Integer> codes = records.stream()
                .map(DivergenceRecord::getProductCode)
                .distinct()
                .toList();

        Map<Integer, Product> productMap = productRepository.findAllById(codes)
                .stream()
                .collect(Collectors.toMap(Product::getCode, p -> p));

        return records.stream()
                .map(r -> toResponse(r, productMap.get(r.getProductCode()), r.getCurrentStock()))
                .toList();
    }

    private DivergenceRecordResponse toResponse(DivergenceRecord record, Product product, Double currentStock) {
        DivergenceRecordResponse response = new DivergenceRecordResponse();
        response.setId(record.getId());
        response.setDate(record.getDate());
        response.setStoreCode(record.getStoreCode());
        response.setProductCode(record.getProductCode());
        response.setTipo(record.getTipo());
        response.setQuantity(record.getQuantity());
        response.setCurrentStock(currentStock);
        response.setSeparatorName(record.getSeparatorName());
        response.setNf(record.getNf());
        response.setObservation(record.getObservation());
        response.setCreatedAt(record.getCreatedAt());

        if (product != null) {
            response.setProductDescription(product.getDescription());
            response.setProductComplement(product.getComplement());
            response.setBarcode(product.getBarcode());
        }

        return response;
    }

    private DivergenceRecord toEntity(DivergenceRecordRequest request) {
        DivergenceRecord record = new DivergenceRecord();
        record.setDate(request.getDate());
        record.setStoreCode(request.getStoreCode());
        record.setProductCode(request.getProductCode());
        record.setTipo(request.getTipo());
        record.setQuantity(request.getQuantity());
        record.setCurrentStock(request.getCurrentStock());
        record.setSeparatorName(request.getSeparatorName());
        record.setNf(request.getNf());
        record.setObservation(request.getObservation());
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }
}