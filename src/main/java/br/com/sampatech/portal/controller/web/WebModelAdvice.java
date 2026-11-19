package br.com.sampatech.portal.controller.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice(basePackages = "br.com.sampatech.portal.controller.web")
public class WebModelAdvice {

    public static final List<String> STATUS_FLOW = List.of("OPEN", "ASSIGNED", "IN_PROGRESS", "VALIDATION", "RESOLVED");

    private static final Map<String, String> STATUS_LABELS = ordered(
            "OPEN", "Aberta",
            "ASSIGNED", "Encaminhada",
            "IN_PROGRESS", "Em andamento",
            "VALIDATION", "Em validação",
            "RESOLVED", "Resolvida");

    private static final Map<String, String> SEVERITY_LABELS = ordered(
            "LOW", "Baixa",
            "MEDIUM", "Média",
            "HIGH", "Alta",
            "CRITICAL", "Crítica");

    private static final Map<String, String> CATEGORY_LABELS = ordered(
            "ROAD_INFRASTRUCTURE", "Infraestrutura viária",
            "ACCESSIBILITY", "Acessibilidade",
            "URBAN_CLEANING", "Limpeza urbana",
            "PUBLIC_LIGHTING", "Iluminação pública");

    private static final Map<String, String> CATEGORY_ICONS = ordered(
            "ROAD_INFRASTRUCTURE", "icon-road",
            "ACCESSIBILITY", "icon-wheelchair",
            "URBAN_CLEANING", "icon-broom",
            "PUBLIC_LIGHTING", "icon-lightbulb");

    private static final Map<String, String> SOURCE_LABELS = ordered(
            "AI", "Câmera + IA",
            "CITIZEN", "Cidadão",
            "AGENT", "Agente municipal");

    private static final Map<String, String> USER_TYPE_LABELS = ordered(
            "CITIZEN", "Cidadão",
            "AGENT", "Agente",
            "ADMIN", "Administrador");

    @ModelAttribute("statusLabels")
    public Map<String, String> statusLabels() {
        return STATUS_LABELS;
    }

    @ModelAttribute("severityLabels")
    public Map<String, String> severityLabels() {
        return SEVERITY_LABELS;
    }

    @ModelAttribute("categoryLabels")
    public Map<String, String> categoryLabels() {
        return CATEGORY_LABELS;
    }

    @ModelAttribute("categoryIcons")
    public Map<String, String> categoryIcons() {
        return CATEGORY_ICONS;
    }

    @ModelAttribute("sourceLabels")
    public Map<String, String> sourceLabels() {
        return SOURCE_LABELS;
    }

    @ModelAttribute("userTypeLabels")
    public Map<String, String> userTypeLabels() {
        return USER_TYPE_LABELS;
    }

    private static Map<String, String> ordered(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }
}
