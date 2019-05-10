package example.api.controller;

import example.application.service.employee.EmployeeQueryService;
import example.infrastructure.datasource.employee.EmployeeDatasource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class EmployeeAPITest {

    @Autowired
    MockMvc mockMvc;

    @SpyBean
    EmployeeQueryService employeeQueryService;

    @SpyBean
    EmployeeDatasource employeeDatasource;

    @Test
    void test() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void employee() throws Exception {
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mailAddress").value("fukawa_teruyoshi_new@example.com"))
        ;
    }

    @Test
    void register() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{'name':'テストユーザー','mailAddress':'test@example.com','phoneNumber':'123-1234-1234'}" .replace('\'', '"'))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("テストユーザー"));
    }

    @Test
    void バリデーションエラー1() throws Exception {
        mockMvc.perform(get("/api/employees/x"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json("{'type':'入力エラー','code':'E00001','message':'入力エラーです。'}"));
    }

    @Test
    void バリデーションエラー2() throws Exception {
        mockMvc.perform(post("/api/employees"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json("{'type':'入力エラー','code':'E00002','message':'入力エラーです。'}"));
    }

    @Test
    void バリデーションエラー3() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{'name':'テストユーザー','mailAddress':'test@example.com','phoneNumber':'123'}" .replace('\'', '"'))
        )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json("{'type':'入力エラー','code':'E00003','message':'入力エラーの項目があります（phoneNumber.value）。'}"));
    }

    @Test
    void プレゼンテーションで例外発生() throws Exception {
        doThrow(RuntimeException.class)
                .when(employeeQueryService)
                .contractingEmployees();

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{'type':'システムエラー','code':'E99999','message':'不明なエラーが発生しました。'}"));
    }

    @Test
    void インフラで例外発生1() throws Exception {
        mockMvc.perform(get("/api/employees/100000"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{'type':'業務エラー','code':'B30001','message':'存在しない従業員番号が指定されました。'}"));
    }

    @Test
    void インフラで例外発生2() throws Exception {
        doThrow(BadSqlGrammarException.class)
                .when(employeeDatasource)
                .findUnderContracts();

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{'type':'システムエラー','code':'E30002','message':'DBアクセスエラーです。'}"));

    }
}