package com.example.jobplatform.service;

import com.example.jobplatform.entity.Skill;
import com.example.jobplatform.entity.User;
import com.example.jobplatform.entity.UserSkill;
import com.example.jobplatform.exception.ResourceNotFoundException;
import com.example.jobplatform.repository.SkillRepository;
import com.example.jobplatform.repository.UserRepository;
import com.example.jobplatform.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSkillService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    public UserSkillService(UserRepository userRepository,
                            SkillRepository skillRepository,
                            UserSkillRepository userSkillRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public UserSkill addSkillToUser(Long userId, Long skillId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found"));

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);

        return userSkillRepository.save(userSkill);
    }

    public List<Skill> getSkillsByUser(Long userId) {

        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);

        return userSkills.stream()
                .map(UserSkill::getSkill)
                .toList();
    }
}