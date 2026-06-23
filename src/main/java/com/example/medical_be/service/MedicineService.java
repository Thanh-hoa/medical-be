package com.example.medical_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.medical_be.dto.res.MedicineRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.entity.Medicine;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.repository.MedicineRepository;
import com.example.medical_be.support.PaginationUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicineService implements IMedicineService {

    MedicineRepository medicineRepository;
    IMessageTranslator messageTranslator;

    @Override
    public PagedResponse<MedicineRes> search(String q, Integer page, Integer limit) {
        int normalizedPage = PaginationUtils.normalizePage(page, messageTranslator);
        int normalizedLimit = PaginationUtils.normalizeLimit(limit, messageTranslator);

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedLimit, Sort.by(Sort.Direction.ASC, "name"));
        Page<Medicine> result = medicineRepository.search(q, pageable);

        return PaginationUtils.buildPagedResponse(
                result.getContent().stream().map(this::toRes).toList(),
                result,
                page,
                normalizedLimit);
    }

    private MedicineRes toRes(Medicine m) {
        return MedicineRes.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .strength(m.getStrength())
                .unit(m.getUnit())
                .dosageForm(m.getDosageForm())
                .description(m.getDescription())
                .build();
    }
}
