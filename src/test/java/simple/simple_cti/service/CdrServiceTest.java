package simple.simple_cti.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
    // findAll()
    // -------------------------------------------------------------------------

    @Test
    void findAll_저장된_CDR을_최신순으로_반환한다() {
        // Given
        Cdr cdr1 = buildCdr("id1");
        Cdr cdr2 = buildCdr("id2");
        when(cdrRepository.findAllByOrderByCalldateDesc()).thenReturn(List.of(cdr1, cdr2));

        // When
        List<Cdr> result = cdrService.findAll();

        // Then
        assertEquals(2, result.size());
        verify(cdrRepository).findAllByOrderByCalldateDesc();
    }

    @Test
    void findAll_CDR이_없으면_빈_목록을_반환한다() {
        // Given
        when(cdrRepository.findAllByOrderByCalldateDesc()).thenReturn(List.of());

        // When
        List<Cdr> result = cdrService.findAll();

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
