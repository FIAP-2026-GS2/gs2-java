package br.com.sampatech.portal.dto.response;

import br.com.sampatech.portal.entity.MunicipalAgency;

public record MunicipalAgencyResponse(
        Long id, String name, String description, String city,
        String state, String subprefecture, Boolean active
) {
    public static MunicipalAgencyResponse from(MunicipalAgency a) {
        return new MunicipalAgencyResponse(a.getId(), a.getName(), a.getDescription(),
                a.getCity(), a.getState(), a.getSubprefecture(), a.getActive());
    }
}