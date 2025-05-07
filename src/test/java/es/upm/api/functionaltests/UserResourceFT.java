package es.upm.api.functionaltests;

import es.upm.api.resources.view.UserDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static es.upm.api.data.entities.Role.*;
import static es.upm.api.resources.UserResource.*;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserResourceFT {
    private final HttpRequestBuilder httpRequestBuilder;

    @Autowired
    UserResourceFT(@Value("${spring.security.oauth2.clients.api-client-id}") String apiClientId, @Value("${spring.security.oauth2.clients.api-client-secret}") String apiClientSecret, TestRestTemplate testRestTemplate) {
        this.httpRequestBuilder = HttpRequestBuilder.create(testRestTemplate, apiClientId, apiClientSecret);
    }

    @Test
    void testReadUser() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID, "aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000").role(ADMIN).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo("66");
        assertThat(response.getBody().getFirstName()).isEqualTo("customer");
    }

    @Test
    void testReadByMobile() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + MOBILE + MOBILE_ID, "66").role(ADMIN).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMobile()).isEqualTo("66");
        assertThat(response.getBody().getFirstName()).isEqualTo("customer");
    }


    @Test
    void testFindAll() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).role(ADMIN).exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testReadUserNotFound() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID, "aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999").role(ADMIN).exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testReadUserUnauthorized() {
        ResponseEntity<UserDto> response = this.httpRequestBuilder
                .get(USERS + ID_ID, "a4093025-cd94-40e0-986a-a15e3ad62ea8").exchange(UserDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateUserWithAdmin() {
        UserDto userDto = UserDto.builder().mobile("666001666").firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testCreateUserConflict() {
        UserDto userDto = UserDto.builder().mobile("666666000").firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testCreateUserBadNumber() {
        UserDto userDto = UserDto.builder().mobile("1").firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testCreateUserWithoutNumber() {
        UserDto userDto = UserDto.builder().mobile(null).firstName("daemon").build();
        ResponseEntity<Void> response = this.httpRequestBuilder
                .post(USERS).body(userDto).role(ADMIN).exchange(Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testReadOperator() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).role(OPERATOR).exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("c1", "c2")
                .doesNotContain("man", "admin");
    }

    @Test
    void testSearch() {
        ResponseEntity<UserDto[]> response = this.httpRequestBuilder
                .get(USERS).param("dni", "c").role(MANAGER).exchange(UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Arrays.stream(response.getBody()).map(UserDto::getFirstName).toList())
                .contains("man")
                .doesNotContain("ope", "admin");
    }

    @Test
    void testSearchDoesNotContainNull() {
        ResponseEntity<String> response = this.httpRequestBuilder
                .get(USERS).role(MANAGER).exchange(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("null");
        log.debug("json: {}", response.getBody());
    }

}
