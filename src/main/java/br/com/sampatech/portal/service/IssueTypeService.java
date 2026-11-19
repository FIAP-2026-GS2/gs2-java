package br.com.sampatech.portal.service;

import br.com.sampatech.portal.dto.request.IssueTypeRequest;
import br.com.sampatech.portal.dto.response.IssueTypeResponse;
import br.com.sampatech.portal.entity.IssueType;
import br.com.sampatech.portal.exception.BusinessException;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.repository.IssueTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueTypeService {

    private final IssueTypeRepository issueTypeRepository;

    @Transactional(readOnly = true)
    public List<IssueTypeResponse> listActive() {
        return issueTypeRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(IssueTypeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<IssueTypeResponse> findAll(Pageable pageable) {
        return issueTypeRepository.findAll(pageable).map(IssueTypeResponse::from);
    }

    @Transactional(readOnly = true)
    public IssueTypeResponse findById(Long id) {
        return IssueTypeResponse.from(getOrThrow(id));
    }

    @Transactional
    public IssueTypeResponse create(IssueTypeRequest req) {
        String name = req.name().trim();
        if (issueTypeRepository.existsByNameIgnoreCase(name)) {
            throw duplicated(name);
        }
        IssueType type = IssueType.builder()
                .name(name)
                .category(req.category())
                .description(clean(req.description()))
                .active(true)
                .build();
        return IssueTypeResponse.from(save(type, name));
    }

    @Transactional
    public IssueTypeResponse update(Long id, IssueTypeRequest req) {
        IssueType type = getOrThrow(id);
        String name = req.name().trim();
        boolean nameChanged = !type.getName().equalsIgnoreCase(name);
        if (nameChanged && issueTypeRepository.existsByNameIgnoreCase(name)) {
            throw duplicated(name);
        }
        type.setName(name);
        type.setCategory(req.category());
        type.setDescription(clean(req.description()));
        return IssueTypeResponse.from(save(type, name));
    }

    @Transactional
    public void deactivate(Long id) {
        getOrThrow(id).setActive(false);
    }

    private IssueType save(IssueType type, String name) {
        try {
            return issueTypeRepository.saveAndFlush(type);
        } catch (DataIntegrityViolationException e) {
            throw duplicated(name);
        }
    }

    private BusinessException duplicated(String name) {
        return new BusinessException("Já existe um tipo com o nome " + name);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private IssueType getOrThrow(Long id) {
        return issueTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de ocorrência " + id + " não encontrado"));
    }
}
