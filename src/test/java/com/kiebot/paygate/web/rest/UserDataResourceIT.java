package com.kiebot.paygate.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kiebot.paygate.IntegrationTest;
import com.kiebot.paygate.domain.UserData;
import com.kiebot.paygate.repository.UserDataRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link UserDataResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UserDataResourceIT {

    private static final Integer DEFAULT_USER_ID = 1;
    private static final Integer UPDATED_USER_ID = 2;

    private static final String DEFAULT_STORE = "AAAAAAAAAA";
    private static final String UPDATED_STORE = "BBBBBBBBBB";

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    private static final String DEFAULT_PAY_GATE_ID = "AAAAAAAAAA";
    private static final String UPDATED_PAY_GATE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_PAY_GATE_SECRET = "AAAAAAAAAA";
    private static final String UPDATED_PAY_GATE_SECRET = "BBBBBBBBBB";

    private static final String DEFAULT_SCRIPT_ID = "AAAAAAAAAA";
    private static final String UPDATED_SCRIPT_ID = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/user-data";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserDataMockMvc;

    private UserData userData;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserData createEntity(EntityManager em) {
        UserData userData = new UserData()
            .userId(DEFAULT_USER_ID)
            .store(DEFAULT_STORE)
            .token(DEFAULT_TOKEN)
            .payGateID(DEFAULT_PAY_GATE_ID)
            .payGateSecret(DEFAULT_PAY_GATE_SECRET)
            .scriptId(DEFAULT_SCRIPT_ID);
        return userData;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserData createUpdatedEntity(EntityManager em) {
        UserData userData = new UserData()
            .userId(UPDATED_USER_ID)
            .store(UPDATED_STORE)
            .token(UPDATED_TOKEN)
            .payGateID(UPDATED_PAY_GATE_ID)
            .payGateSecret(UPDATED_PAY_GATE_SECRET)
            .scriptId(UPDATED_SCRIPT_ID);
        return userData;
    }

    @BeforeEach
    public void initTest() {
        userData = createEntity(em);
    }

    @Test
    @Transactional
    void createUserData() throws Exception {
        int databaseSizeBeforeCreate = userDataRepository.findAll().size();
        // Create the UserData
        restUserDataMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isCreated());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeCreate + 1);
        UserData testUserData = userDataList.get(userDataList.size() - 1);
        assertThat(testUserData.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testUserData.getStore()).isEqualTo(DEFAULT_STORE);
        assertThat(testUserData.getToken()).isEqualTo(DEFAULT_TOKEN);
        assertThat(testUserData.getPayGateID()).isEqualTo(DEFAULT_PAY_GATE_ID);
        assertThat(testUserData.getPayGateSecret()).isEqualTo(DEFAULT_PAY_GATE_SECRET);
        assertThat(testUserData.getScriptId()).isEqualTo(DEFAULT_SCRIPT_ID);
    }

    @Test
    @Transactional
    void createUserDataWithExistingId() throws Exception {
        // Create the UserData with an existing ID
        userData.setId(1L);

        int databaseSizeBeforeCreate = userDataRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserDataMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllUserData() throws Exception {
        // Initialize the database
        userDataRepository.saveAndFlush(userData);

        // Get all the userDataList
        restUserDataMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(userData.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].store").value(hasItem(DEFAULT_STORE)))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)))
            .andExpect(jsonPath("$.[*].payGateID").value(hasItem(DEFAULT_PAY_GATE_ID)))
            .andExpect(jsonPath("$.[*].payGateSecret").value(hasItem(DEFAULT_PAY_GATE_SECRET)))
            .andExpect(jsonPath("$.[*].scriptId").value(hasItem(DEFAULT_SCRIPT_ID)));
    }

    @Test
    @Transactional
    void getUserData() throws Exception {
        // Initialize the database
        userDataRepository.saveAndFlush(userData);

        // Get the userData
        restUserDataMockMvc
            .perform(get(ENTITY_API_URL_ID, userData.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(userData.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.store").value(DEFAULT_STORE))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN))
            .andExpect(jsonPath("$.payGateID").value(DEFAULT_PAY_GATE_ID))
            .andExpect(jsonPath("$.payGateSecret").value(DEFAULT_PAY_GATE_SECRET))
            .andExpect(jsonPath("$.scriptId").value(DEFAULT_SCRIPT_ID));
    }

    @Test
    @Transactional
    void getNonExistingUserData() throws Exception {
        // Get the userData
        restUserDataMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewUserData() throws Exception {
        // Initialize the database
        userDataRepository.saveAndFlush(userData);

        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();

        // Update the userData
        UserData updatedUserData = userDataRepository.findById(userData.getId()).get();
        // Disconnect from session so that the updates on updatedUserData are not directly saved in db
        em.detach(updatedUserData);
        updatedUserData
            .userId(UPDATED_USER_ID)
            .store(UPDATED_STORE)
            .token(UPDATED_TOKEN)
            .payGateID(UPDATED_PAY_GATE_ID)
            .payGateSecret(UPDATED_PAY_GATE_SECRET)
            .scriptId(UPDATED_SCRIPT_ID);

        restUserDataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedUserData.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedUserData))
            )
            .andExpect(status().isOk());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
        UserData testUserData = userDataList.get(userDataList.size() - 1);
        assertThat(testUserData.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testUserData.getStore()).isEqualTo(UPDATED_STORE);
        assertThat(testUserData.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testUserData.getPayGateID()).isEqualTo(UPDATED_PAY_GATE_ID);
        assertThat(testUserData.getPayGateSecret()).isEqualTo(UPDATED_PAY_GATE_SECRET);
        assertThat(testUserData.getScriptId()).isEqualTo(UPDATED_SCRIPT_ID);
    }

    @Test
    @Transactional
    void putNonExistingUserData() throws Exception {
        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();
        userData.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserDataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, userData.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchUserData() throws Exception {
        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();
        userData.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserDataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamUserData() throws Exception {
        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();
        userData.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserDataMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateUserDataWithPatch() throws Exception {
        // Initialize the database
        userDataRepository.saveAndFlush(userData);

        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();

        // Update the userData using partial update
        UserData partialUpdatedUserData = new UserData();
        partialUpdatedUserData.setId(userData.getId());

        partialUpdatedUserData.payGateID(UPDATED_PAY_GATE_ID).scriptId(UPDATED_SCRIPT_ID);

        restUserDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserData.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUserData))
            )
            .andExpect(status().isOk());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
        UserData testUserData = userDataList.get(userDataList.size() - 1);
        assertThat(testUserData.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testUserData.getStore()).isEqualTo(DEFAULT_STORE);
        assertThat(testUserData.getToken()).isEqualTo(DEFAULT_TOKEN);
        assertThat(testUserData.getPayGateID()).isEqualTo(UPDATED_PAY_GATE_ID);
        assertThat(testUserData.getPayGateSecret()).isEqualTo(DEFAULT_PAY_GATE_SECRET);
        assertThat(testUserData.getScriptId()).isEqualTo(UPDATED_SCRIPT_ID);
    }

    @Test
    @Transactional
    void fullUpdateUserDataWithPatch() throws Exception {
        // Initialize the database
        userDataRepository.saveAndFlush(userData);

        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();

        // Update the userData using partial update
        UserData partialUpdatedUserData = new UserData();
        partialUpdatedUserData.setId(userData.getId());

        partialUpdatedUserData
            .userId(UPDATED_USER_ID)
            .store(UPDATED_STORE)
            .token(UPDATED_TOKEN)
            .payGateID(UPDATED_PAY_GATE_ID)
            .payGateSecret(UPDATED_PAY_GATE_SECRET)
            .scriptId(UPDATED_SCRIPT_ID);

        restUserDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUserData.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedUserData))
            )
            .andExpect(status().isOk());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
        UserData testUserData = userDataList.get(userDataList.size() - 1);
        assertThat(testUserData.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testUserData.getStore()).isEqualTo(UPDATED_STORE);
        assertThat(testUserData.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testUserData.getPayGateID()).isEqualTo(UPDATED_PAY_GATE_ID);
        assertThat(testUserData.getPayGateSecret()).isEqualTo(UPDATED_PAY_GATE_SECRET);
        assertThat(testUserData.getScriptId()).isEqualTo(UPDATED_SCRIPT_ID);
    }

    @Test
    @Transactional
    void patchNonExistingUserData() throws Exception {
        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();
        userData.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, userData.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchUserData() throws Exception {
        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();
        userData.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserDataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isBadRequest());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamUserData() throws Exception {
        int databaseSizeBeforeUpdate = userDataRepository.findAll().size();
        userData.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserDataMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(userData))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the UserData in the database
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteUserData() throws Exception {
        // Initialize the database
        userDataRepository.saveAndFlush(userData);

        int databaseSizeBeforeDelete = userDataRepository.findAll().size();

        // Delete the userData
        restUserDataMockMvc
            .perform(delete(ENTITY_API_URL_ID, userData.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UserData> userDataList = userDataRepository.findAll();
        assertThat(userDataList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
