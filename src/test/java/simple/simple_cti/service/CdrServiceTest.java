package simple.simple_cti.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import simple.simple_cti.domain.Cdr;
import simple.simple_cti.repository.CdrRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CdrService 단위 테스트
 *
 * [A방식 Mock 전략]
 * - CdrService: 실제 객체 — 저장/중복 처리/조회 로직이 실제로 실행됨
 * - CdrRepository: Mockito.mock() — DB 경계 (유일한 외부 시스템 Mock)
 */
@ExtendWith(MockitoExtension.class)
class CdrServiceTest {

    private CdrRepository cdrRepository;
    private CdrService cdrService;

    @BeforeEach
    void setUp() {
        cdrRepository = mock(CdrRepository.class);
        cdrService = new CdrService(cdrRepository);
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Test
    void save_신규_uniqueid이면_repository에_저장한다() {
        // Given
        Cdr cdr = buildCdr("1234567890.1");
        when(cdrRepository.findByUniqueid("1234567890.1")).thenReturn(Optional.empty());

        // When
        cdrService.save(cdr);

        // Then: 중복 아니므로 repository.save() 1회 호출
        verify(cdrRepository).save(cdr);
    }

    @Test
    void save_중복_uniqueid이면_저장하지_않는다() {
        // Given
        Cdr cdr = buildCdr("1234567890.1");
        Cdr existing = buildCdr("1234567890.1");
        when(cdrRepository.findByUniqueid("1234567890.1")).thenReturn(Optional.of(existing));

        // When
        cdrService.save(cdr);

        // Then: 중복이므로 repository.save() 호출하지 않음
        verify(cdrRepository, never()).save(any());
    }

    @Test
    void save_repository_예외_발생해도_예외를_전파하지_않는다() {
        // Given
        Cdr cdr = buildCdr("1234567890.2");
        when(cdrRepository.findByUniqueid("1234567890.2")).thenReturn(Optional.empty());
        when(cdrRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        // When & Then: 예외가 전파되지 않아야 함
        assertDoesNotThrow(() -> cdrService.save(cdr));
    }

    // -------------------------------------------------------------------------
    // findByFilter()
    // -------------------------------------------------------------------------

    @Test
    void findByFilter_결과가_있으면_Page를_반환한다() {
        // Given
        Cdr cdr1 = buildCdr("id1");
        Cdr cdr2 = buildCdr("id2");
        Page<Cdr> page = new PageImpl<>(List.of(cdr1, cdr2), PageRequest.of(0, 20), 2);
        when(cdrRepository.findByFilter(any(), any(), any())).thenReturn(page);

        // When
        Page<Cdr> result = cdrService.findByFilter("", "", 0);

        // Then
        assertEquals(2, result.getTotalElements());
        verify(cdrRepository).findByFilter("", "", PageRequest.of(0, 20));
    }

    @Test
    void findByFilter_CDR이_없으면_빈_Page를_반환한다() {
        // Given
        Page<Cdr> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(cdrRepository.findByFilter(any(), any(), any())).thenReturn(emptyPage);

        // When
        Page<Cdr> result = cdrService.findByFilter("", "", 0);

        // Then
        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Cdr buildCdr(String uniqueid) {
        Cdr cdr = new Cdr();
        cdr.setUniqueid(uniqueid);
        cdr.setSrc("01012345678");
        cdr.setDst("01098765432");
        cdr.setCalldate(LocalDateTime.now());
        return cdr;
    }
}
