package br.com.sampatech.portal.service;

import br.com.sampatech.portal.dto.request.MunicipalAgencyRequest;
import br.com.sampatech.portal.dto.response.MunicipalAgencyResponse;
import br.com.sampatech.portal.entity.MunicipalAgency;
import br.com.sampatech.portal.exception.BusinessException;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.repository.MunicipalAgencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MunicipalAgencyService {

    private final MunicipalAgencyRepository agencyRepository;

    @Transactional(readOnly = true)
    public Page<MunicipalAgencyResponse> findAll(Pageable pageable) {
        return agencyRepository.findAll(pageable).map(MunicipalAgencyResponse::from);
    }

    @Transactional(readOnly = true)
    public MunicipalAgencyResponse findById(Long id) {
        return MunicipalAgencyResponse.from(getOrThrow(id));
    }

    @Transactional
    public MunicipalAgencyResponse create(MunicipalAgencyRequest req) {
        MunicipalAgency agency = MunicipalAgency.builder()
                .name(req.name().trim())
                .description(req.description())
                .city(req.city().trim())
                .state(req.state())
                .subprefecture(req.subprefecture())
                .active(true)
                .build();
        try {
            return MunicipalAgencyResponse.from(agencyRepository.saveAndFlush(agency));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já existe um órgão com esse nome nessa cidade/UF");
        }
    }

    @Transactional
    public MunicipalAgencyResponse update(Long id, MunicipalAgencyRequest req) {
        MunicipalAgency agency = getOrThrow(id);
        agency.setName(req.name().trim());
        agency.setDescription(req.description());
        agency.setCity(req.city().trim());
        agency.setState(req.state());
        agency.setSubprefecture(req.subprefecture());
        try {
            return MunicipalAgencyResponse.from(agencyRepository.saveAndFlush(agency));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já existe um órgão com esse nome nessa cidade/UF");
        }
    }

    @Transactional
    public void deactivate(Long id) {
        getOrThrow(id).setActive(false);
    }

    private MunicipalAgency getOrThrow(Long id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Órgão " + id + " não encontrado"));
    }
}