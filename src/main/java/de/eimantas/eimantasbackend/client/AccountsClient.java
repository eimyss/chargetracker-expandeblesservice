package de.eimantas.eimantasbackend.client;


import de.eimantas.eimantasbackend.entities.dto.AccountOverViewDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;

@FeignClient(value = "${feign.client.config.account.name}", configuration = AccountsClientConfig.class)
public interface AccountsClient {

    @GetMapping("/account/overview/{id}")
    ResponseEntity<AccountOverViewDTO> readAccountOverview(@PathVariable(name = "id") long id);

    @GetMapping("/account/list/id")
    Collection<Long> getAccountList();
}

