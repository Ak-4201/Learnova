package com.learnova;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests verifying LMS requirements:
 * - Auth: Signup, Login, JWT, roles
 * - Course listing and details (thumbnail, instructor, description)
 * - Lessons with YouTube URL, order, section
 * - Enrollment (student-course)
 * - Progress (completed, percentage, last watched)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LmsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String studentToken;
    private static Long firstCourseId;
    private static Long firstLessonId;

    private void ensureStudentToken() throws Exception {
        if (studentToken != null) return;
        String body = """
            {"email":"student1@test.com","password":"pass1234","fullName":"Test Student"}
            """;
        String json = mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        studentToken = objectMapper.readTree(json).get("token").asText();
    }

    // --- Authentication ---
    @Test
    @Order(1)
    void signup_returnsTokenAndUserInfo() throws Exception {
        String body = """
            {"email":"student1@test.com","password":"pass1234","fullName":"Test Student"}
            """;
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.email").value("student1@test.com"))
            .andExpect(jsonPath("$.fullName").value("Test Student"))
            .andExpect(jsonPath("$.role").exists())
            .andExpect(jsonPath("$.userId").isNumber());
        String json = result.andReturn().getResponse().getContentAsString();
        studentToken = objectMapper.readTree(json).get("token").asText();
    }

    @Test
    @Order(2)
    void login_withValidCreds_returnsToken() throws Exception {
        // Use seeded instructor
        String body = "{\"email\":\"instructor@learnova.com\",\"password\":\"instructor123\"}";
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.email").value("instructor@learnova.com"));
    }

    @Test
    @Order(3)
    void login_withInvalidCreds_returns401() throws Exception {
        String body = "{\"email\":\"wrong@test.com\",\"password\":\"wrong\"}";
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void signup_duplicateEmail_returnsBadRequest() throws Exception {
        String body = """
            {"email":"student1@test.com","password":"other123","fullName":"Other"}
            """;
        mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isBadRequest());
    }

    // --- Course listing (no auth required) ---
    @Test
    @Order(10)
    void listCourses_returnsCoursesWithThumbnailInstructorDescription() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/courses"));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        String json = result.andReturn().getResponse().getContentAsString();
        List<?> list = objectMapper.readValue(json, List.class);
        Map<?, ?> first = (Map<?, ?>) list.get(0);
        firstCourseId = ((Number) first.get("id")).longValue();
        Assertions.assertNotNull(first.get("title"));
        Assertions.assertNotNull(first.get("instructorName"));
        Assertions.assertNotNull(first.get("description"));
        Assertions.assertNotNull(first.get("thumbnailUrl"));
        Assertions.assertNotNull(first.get("totalLessons"));
        Assertions.assertNotNull(first.get("enrolled"));
    }

    @Test
    @Order(11)
    void getCourseById_returnsDetailsWithWhatYouWillLearnAndDuration() throws Exception {
        if (firstCourseId == null) firstCourseId = 1L;
        mockMvc.perform(get("/api/courses/" + firstCourseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(firstCourseId))
            .andExpect(jsonPath("$.title").exists())
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.whatYouWillLearn").exists())
            .andExpect(jsonPath("$.instructorName").exists())
            .andExpect(jsonPath("$.totalLessons").isNumber())
            .andExpect(jsonPath("$.totalDurationSeconds").exists());
    }

    @Test
    @Order(12)
    void getCourseById_invalidId_returns404() throws Exception {
        mockMvc.perform(get("/api/courses/99999"))
            .andExpect(status().isNotFound());
    }

    // --- Lessons (structure: sections -> lessons, YouTube URL) ---
    @Test
    @Order(20)
    void getLessonsByCourseId_returnsLessonsWithYoutubeUrlOrderAndSection() throws Exception {
        if (firstCourseId == null) firstCourseId = 1L;
        ResultActions result = mockMvc.perform(get("/api/courses/" + firstCourseId + "/lessons"));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.courseId").value(firstCourseId))
            .andExpect(jsonPath("$.lessons").isArray())
            .andExpect(jsonPath("$.totalLessons").isNumber())
            .andExpect(jsonPath("$.progressPercent").isNumber())
            .andExpect(jsonPath("$.enrolled").exists());
        String json = result.andReturn().getResponse().getContentAsString();
        Map<?, ?> res = objectMapper.readValue(json, Map.class);
        List<?> lessons = (List<?>) res.get("lessons");
        if (!lessons.isEmpty()) {
            Map<?, ?> lesson = (Map<?, ?>) lessons.get(0);
            firstLessonId = ((Number) lesson.get("id")).longValue();
            Assertions.assertNotNull(lesson.get("title"));
            Assertions.assertNotNull(lesson.get("orderNumber"));
            Assertions.assertNotNull(lesson.get("youtubeUrl"));
            Assertions.assertTrue(lesson.get("youtubeUrl").toString().contains("youtube.com"));
            Assertions.assertNotNull(lesson.get("sectionTitle"));
            Assertions.assertNotNull(lesson.get("completed"));
        }
    }

    @Test
    @Order(21)
    void getLessonById_returnsYoutubeUrlForIframe() throws Exception {
        if (firstLessonId == null) firstLessonId = 1L;
        mockMvc.perform(get("/api/lessons/" + firstLessonId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(firstLessonId))
            .andExpect(jsonPath("$.youtubeUrl").exists())
            .andExpect(jsonPath("$.title").exists());
    }

    // --- Enrollment (auth required) ---
    @Test
    @Order(30)
    void enroll_withoutAuth_returns4xxForbidden() throws Exception {
        if (firstCourseId == null) firstCourseId = 1L;
        mockMvc.perform(post("/api/courses/" + firstCourseId + "/enroll"))
            .andExpect(status().is4xxClientError()); // 403 Forbidden when unauthenticated
    }

    @Test
    @Order(31)
    void enroll_withAuth_returns200() throws Exception {
        ensureStudentToken();
        if (firstCourseId == null) firstCourseId = 1L;
        mockMvc.perform(post("/api/courses/" + firstCourseId + "/enroll")
            .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk());
    }

    @Test
    @Order(32)
    void getCourseById_afterEnroll_showsEnrolledTrue() throws Exception {
        if (firstCourseId == null) firstCourseId = 1L;
        mockMvc.perform(get("/api/courses/" + firstCourseId)
            .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enrolled").value(true));
    }

    // --- Progress (completed, percentage, last watched) ---
    @Test
    @Order(40)
    void recordProgress_withoutAuth_returns4xxForbidden() throws Exception {
        if (firstCourseId == null) firstCourseId = 1L;
        if (firstLessonId == null) firstLessonId = 1L;
        String body = "{\"lessonId\":" + firstLessonId + ",\"completed\":true}";
        mockMvc.perform(post("/api/courses/" + firstCourseId + "/progress")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().is4xxClientError()); // 403 when unauthenticated
    }

    @Test
    @Order(41)
    void recordProgress_withAuth_returnsProgressPercentAndCompletedCount() throws Exception {
        ensureStudentToken();
        if (firstCourseId == null) firstCourseId = 1L;
        if (firstLessonId == null) firstLessonId = 1L;
        String body = "{\"lessonId\":" + firstLessonId + ",\"completed\":true}";
        mockMvc.perform(post("/api/courses/" + firstCourseId + "/progress")
            .header("Authorization", "Bearer " + studentToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonId").value(firstLessonId))
            .andExpect(jsonPath("$.completed").value(true))
            .andExpect(jsonPath("$.completedCount").isNumber())
            .andExpect(jsonPath("$.totalLessons").isNumber())
            .andExpect(jsonPath("$.progressPercent").isNumber());
    }

    @Test
    @Order(42)
    void getLessons_afterProgress_showsCompletedAndProgressPercent() throws Exception {
        if (firstCourseId == null) firstCourseId = 1L;
        mockMvc.perform(get("/api/courses/" + firstCourseId + "/lessons")
            .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.progressPercent").value(greaterThanOrEqualTo(0)))
            .andExpect(jsonPath("$.completedCount").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.lastWatchedLessonId").exists());
    }

    @Test
    @Order(43)
    void updateLastWatched_withAuth_returns200() throws Exception {
        ensureStudentToken();
        if (firstCourseId == null) firstCourseId = 1L;
        if (firstLessonId == null) firstLessonId = 1L;
        mockMvc.perform(post("/api/courses/" + firstCourseId + "/lessons/" + firstLessonId + "/watch")
            .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk());
    }

    // --- Dashboard enrollments (auth required) ---
    @Test
    @Order(50)
    void getDashboardEnrollments_withoutAuth_returns4xxForbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/enrollments"))
            .andExpect(status().is4xxClientError()); // 403 when unauthenticated
    }

    @Test
    @Order(51)
    void getDashboardEnrollments_withAuth_returnsEnrolledCoursesWithProgress() throws Exception {
        ensureStudentToken();
        mockMvc.perform(get("/api/dashboard/enrollments")
            .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].courseId").exists())
            .andExpect(jsonPath("$[0].title").exists())
            .andExpect(jsonPath("$[0].progressPercent").exists())
            .andExpect(jsonPath("$[0].completedLessons").exists())
            .andExpect(jsonPath("$[0].totalLessons").exists());
    }
}
