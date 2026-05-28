package pl.easyoffer.offer_service.model.dto.nofluffjobs;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class NofluffjobsPostingsWrapper {

    private Integer totalPages;
    private Set<NofluffjobsPosting> postings;
    private Requirements requirements;

    @Data
    @Builder
    public static class Requirements {
        private List<Item> musts;
        private List<Item> nices;
        private String description;
        private List<Language> languages;

        @Data
        @Builder
        public static class Item {
            private String value;
            private String type;
        }

        @Data
        @Builder
        public static class Language {
            private String type;
            private String code;
        }
    }

}
