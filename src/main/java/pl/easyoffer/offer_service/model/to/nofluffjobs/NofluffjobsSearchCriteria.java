package pl.easyoffer.offer_service.model.to.nofluffjobs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NofluffjobsSearchCriteria {

    private String criteria;
    private int pageSize;
    private String rawSearch;
    private Url url;
    private boolean withSalaryMatch;

    @Data
    @Builder
    public static class Url {

        private String searchParam;

    }

}
