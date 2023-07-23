package com.server.schedule.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.server.domain.member.entity.Member;
import com.server.domain.member.service.MemberService;
import com.server.domain.place.dto.PlaceDto;
import com.server.domain.place.entity.Place;
import com.server.domain.place.mapper.PlaceMapper;
import com.server.domain.place.service.PlaceService;
import com.server.domain.region.entity.Region;
import com.server.domain.schedule.dto.ScheduleDto;
import com.server.domain.schedule.entity.Schedule;
import com.server.domain.schedule.entity.SchedulePlace;
import com.server.domain.schedule.mapper.ScheduleMapper;
import com.server.domain.schedule.service.SchedulePlaceService;
import com.server.domain.schedule.service.ScheduleService;
import com.server.global.auth.jwt.JwtTokenizer;
import com.server.helper.LocalDateAdapter;
import com.server.helper.StubData;
import com.server.helper.StubData.MockPlace;
import com.server.helper.StubData.MockSchedule;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScheduleControllerTest {
    private static final String BASE_URL = "/api/schedules";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private PlaceMapper placeMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    // @Autowired
    private final Gson gson = new GsonBuilder()
        // .setPrettyPrinting()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .create();

    @MockBean
    private MemberService memberService;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private PlaceService placeService;

    @MockBean
    private SchedulePlaceService schedulePlaceService;

    private String token;

    private LocalDateTime now;

    @BeforeAll
    public void init() {
        token = StubData.MockSecurity.getValidAccessToken(jwtTokenizer.getSecretKey());
    }

    @BeforeEach
    public void setTime() {
        now = LocalDateTime.now().withNano(0);
    }

    @Test
    @DisplayName("여행 일정 등록")
    void postScheduleTest() throws Exception {
        // given
        ScheduleDto.Post postDto = MockSchedule.postDto;
        List<List<PlaceDto.Post>> placeDtoLists = MockPlace.postDtoLists;
        postDto.setPlaces(placeDtoLists);

        String requestBody = gson.toJson(postDto);

        // List<List<Place>> placeLists = placeMapper.postDtoListsToPlaceLists(placeDtoLists);
        Schedule schedule = new Schedule();
        schedule.setScheduleId(1L);

        Member member = Member.builder()
            .memberId(1L)
            .build();

        given(memberService.findMember(Mockito.anyLong())).willReturn(member);
        given(scheduleService.saveSchedule(Mockito.any(Schedule.class))).willReturn(schedule);
        given(placeService.savePlaceLists(Mockito.any(Schedule.class), Mockito.anyList()))
            .willReturn(null);
        // given(schedulePlaceSedrvice.saveSchedulePlaces(Mockito.<SchedulePlace>anyList())).willReturn(null);
        doNothing().when(scheduleService).sendKakaoMessage(Mockito.any(Schedule.class), Mockito.any(Member.class));

        // when
        ResultActions actions = mockMvc.perform(
            post(BASE_URL + "/write")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        actions
            .andExpect(status().isCreated())
            .andExpect(header().string("Location",
                is(startsWith("/api/schedules"))))
            .andDo(
                document("여행 일정 등록",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Schedule")
                            .description("여행 일정 등록")
                            // .requestFields(List.of())
                            // .responseFields(List.of())
                            .build())));
    }

    @Test
    @DisplayName("여행 일정 수정")
    void patchScheduleTest() throws Exception {
        // given
        Member member = Member.builder()
            .memberId(1L)
            .nickname("관리자")
            .build();

        List<List<PlaceDto.Post>> placeDtoLists = MockPlace.postDtoLists;
        ScheduleDto.Post postDto = StubData.MockSchedule.postDto;
        postDto.setPlaces(placeDtoLists);

        List<SchedulePlace> schedulePlaces = StubData.MockPlace.schedulePlaces;

        Region region = new Region();
        region.setEngName("seoul");
        region.setKorName("서울");

        Schedule schedule = scheduleMapper.postDtoToSchedule(postDto);
        schedule.setScheduleId(1L);
        schedule.setRegion(region);
        schedule.setPeriod(3);
        schedule.setSchedulePlaces(schedulePlaces);
        schedule.setMember(member);
        schedule.setCreatedAt(now);
        schedule.setModifiedAt(now);

        String requestBody = gson.toJson(postDto);

        given(scheduleService.updateSchedule(Mockito.any(Schedule.class))).willReturn(schedule);
        doNothing().when(scheduleService).deleteSchedule(1);
        given(placeService.savePlaceLists(Mockito.any(Schedule.class), Mockito.anyList()))
            .willReturn(schedulePlaces);

        // when
        ResultActions actions = mockMvc.perform(
            patch(BASE_URL + "/{scheduleId}/edit", 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions
            .andExpect(status().isOk())
            .andDo(
                document("여행 일정 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Schedule")
                            .description("여행 일정 수정")
                            // .requestFields(List.of())
                            // .responseFields(List.of())
                            .build())));
    }

    @Test
    @DisplayName("여행 일정 조회")
    void getScheduleTest() throws Exception {
        // given
        Member member = Member.builder()
            .memberId(1L)
            .nickname("관리자")
            .build();

        ScheduleDto.Post postDto = StubData.MockSchedule.postDto;
        List<SchedulePlace> schedulePlaces = StubData.MockPlace.schedulePlaces;

        Region region = new Region();
        region.setEngName("seoul");
        region.setKorName("서울");

        Schedule schedule = scheduleMapper.postDtoToSchedule(postDto);
        schedule.setScheduleId(1L);
        schedule.setRegion(region);
        schedule.setPeriod(3);
        schedule.setSchedulePlaces(schedulePlaces);
        schedule.setMember(member);
        schedule.setCreatedAt(now);
        schedule.setModifiedAt(now);

        given(scheduleService.findSchedule(Mockito.anyLong())).willReturn(schedule);

        // when
        ResultActions actions = mockMvc.perform(
            get(BASE_URL + "/{scheduleId}", 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions
            .andExpect(status().isOk())
            .andDo(
                document("여행 일정 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Schedule")
                            .description("여행 일정 조회")
                            // .requestFields(List.of())
                            // .responseFields(List.of())
                            .build())));
    }

    @Test
    @DisplayName("여행 일정의 여행지 조회")
    void getPlacesByScheduleIdTest() throws Exception {
        // given
        ScheduleDto.Post postDto = StubData.MockSchedule.postDto;
        List<SchedulePlace> schedulePlaces = StubData.MockPlace.schedulePlaces;
        Schedule schedule = scheduleMapper.postDtoToSchedule(postDto);
        schedule.setScheduleId(1L);
        schedule.setPeriod(3);
        schedule.setSchedulePlaces(schedulePlaces);

        given(scheduleService.findSchedule(Mockito.anyLong())).willReturn(schedule);

        // when
        ResultActions actions = mockMvc.perform(
            get(BASE_URL + "/{scheduleId}/places", 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions
            .andExpect(status().isOk())
            .andDo(
                document("여행 일정의 여행지 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Schedule")
                            .description("여행 일정의 여행지 조회")
                            // .requestFields(List.of())
                            // .responseFields(List.of())
                            .build())));
    }

    @Test
    @DisplayName("여행 일정 삭제")
    void deleteScheduleTest() throws Exception {
        // given
        doNothing().when(scheduleService).deleteSchedule(1);

        // when
        ResultActions actions = mockMvc.perform(
            delete(BASE_URL + "/{scheduleId}", 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        // then
        actions
            .andExpect(status().isNoContent())
            .andDo(
                document("여행 일정 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Schedule")
                            .description("일정 삭제")
                            // .requestFields(List.of())
                            // .responseFields(List.of())
                            .build())));
    }
}
