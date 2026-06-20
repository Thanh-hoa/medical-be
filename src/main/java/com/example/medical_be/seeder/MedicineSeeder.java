package com.example.medical_be.seeder;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.medical_be.entity.Medicine;
import com.example.medical_be.repository.MedicineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineSeeder implements ISeeder {

    private final MedicineRepository medicineRepository;

    @Override
    public void seed() {
        log.info("Seeding medicines...");
        List<Medicine> medicines = dataMedicines();
        int created = 0;
        for (Medicine m : medicines) {
            if (!medicineRepository.existsByCode(m.getCode())) {
                medicineRepository.save(m);
                created++;
            }
        }
        log.info("Seeded {} medicines", created);
    }

    private List<Medicine> dataMedicines() {
        return List.of(
            Medicine.builder().code("MED-0001").name("Paracetamol").strength("500mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0002").name("Paracetamol").strength("325mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0003").name("Amoxicillin").strength("500mg").unit("viên").dosageForm("viên nang").build(),
            Medicine.builder().code("MED-0004").name("Amoxicillin").strength("250mg").unit("viên").dosageForm("viên nang").build(),
            Medicine.builder().code("MED-0005").name("Vitamin C").strength("500mg").unit("viên").dosageForm("viên sủi").build(),
            Medicine.builder().code("MED-0006").name("Vitamin C").strength("1000mg").unit("viên").dosageForm("viên sủi").build(),
            Medicine.builder().code("MED-0007").name("Loratadine").strength("10mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0008").name("Cetirizine").strength("10mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0009").name("Omeprazole").strength("20mg").unit("viên").dosageForm("viên nang").build(),
            Medicine.builder().code("MED-0010").name("Omeprazole").strength("40mg").unit("viên").dosageForm("viên nang").build(),
            Medicine.builder().code("MED-0011").name("Ibuprofen").strength("400mg").unit("viên").dosageForm("viên bao phim").build(),
            Medicine.builder().code("MED-0012").name("Ibuprofen").strength("200mg").unit("viên").dosageForm("viên bao phim").build(),
            Medicine.builder().code("MED-0013").name("Metformin").strength("500mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0014").name("Metformin").strength("850mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0015").name("Salbutamol").strength("4mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0016").name("ORS").strength(null).unit("gói").dosageForm("bột pha uống").description("Oresol bù điện giải").build(),
            Medicine.builder().code("MED-0017").name("Azithromycin").strength("500mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0018").name("Doxycycline").strength("100mg").unit("viên").dosageForm("viên nang").build(),
            Medicine.builder().code("MED-0019").name("Clarithromycin").strength("500mg").unit("viên").dosageForm("viên bao phim").build(),
            Medicine.builder().code("MED-0020").name("Pantoprazole").strength("40mg").unit("viên").dosageForm("viên tan ruột").build(),
            Medicine.builder().code("MED-0021").name("Atorvastatin").strength("20mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0022").name("Amlodipine").strength("5mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0023").name("Losartan").strength("50mg").unit("viên").dosageForm("viên nén").build(),
            Medicine.builder().code("MED-0024").name("Loperamide").strength("2mg").unit("viên").dosageForm("viên nang").build(),
            Medicine.builder().code("MED-0025").name("Diclofenac").strength("50mg").unit("viên").dosageForm("viên bao tan ruột").build()
        );
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
