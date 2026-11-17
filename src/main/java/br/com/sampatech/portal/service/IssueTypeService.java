package br.com.sampatech.portal.service;

import br.com.sampatech.portal.dto.request.IssueTypeRequest;
import br.com.sampatech.portal.dto.response.IssueTypeResponse;
import br.com.sampatech.portal.entity.IssueType;
import br.com.sampatech.portal.exception.BusinessException;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.repository.IssueTypeRepository;
import lombok.RequiredArgsConstructor;
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
        if (issueTypeRepository.existsByNameIgnoreCase(req.name())) {
            throw new BusinessException("Já existe um tipo com o nome " + req.name());
        }
        IssueType type = IssueType.builder()
                .name(req.name().trim())
                .category(req.category())
                .description(req.description())
                .active(true)
                .build();
        return IssueTypeResponse.from(issueTypeRepository.save(type));
    }

    @Transactional
    public IssueTypeResponse update(Long id, IssueTypeRequest req) {
        IssueType type = getOrThrow(id);
        boolean nameChanged = !type.getName().equalsIgnoreCase(req.name());
        if (nameChanged && issueTypeRepository.existsByNameIgnoreCase(req.name())) {
            throw new BusinessException("Já existe um tipo com o nome " + req.name());
        }
        type.setName(req.name().trim());
        type.setCategory(req.category());
        type.setDescription(req.description());
        return IssueTypeResponse.from(type);
    }

    @Transactional
    public void deactivate(Long id) {
        getOrThrow(id).setActive(false);
    }

    private IssueType getOrThrow(Long id) {
        return issueTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de ocorrência " + id + " não encontrado"));
    }
}