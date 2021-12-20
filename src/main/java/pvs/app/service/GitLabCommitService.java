package pvs.app.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pvs.app.dao.GitLabCommitDAO;
import pvs.app.dto.GitLabCommitDTO;
import pvs.app.entity.GitlabCommit;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

@Service
public class GitLabCommitService {
    private final GitLabCommitDAO gitlabCommitDAO;
    private final ModelMapper modelMapper;

    GitLabCommitService(GitLabCommitDAO gitlabCommitDAO, ModelMapper modelMapper) {
        this.gitlabCommitDAO = gitlabCommitDAO;
        this.modelMapper = modelMapper;
    }

    public void save(GitLabCommitDTO gitlabCommitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(gitlabCommitDTO, GitlabCommit.class);
        gitlabCommitDAO.save(gitlabCommit);
    }

    public boolean checkIfExist(GitLabCommitDTO gitlabCommitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(gitlabCommitDTO, GitlabCommit.class);
        List<GitlabCommit> entities = gitlabCommitDAO.findByRepoOwnerAndRepoName(gitlabCommit.getRepoOwner(), gitlabCommit.getRepoName());
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        for (GitlabCommit gitlabCommits : entities) {
            if (String.valueOf(gitlabCommits.getCommittedDate()).equals(sdFormat.format(gitlabCommit.getCommittedDate()))) {
                return true;
            }
        }
        return false;
    }

    public List<GitLabCommitDTO> getAllCommits(String repoOwner, String repoName) {
        List<GitlabCommit> entities = gitlabCommitDAO.findByRepoOwnerAndRepoName(repoOwner, repoName);
        List<GitLabCommitDTO> githubCommitDTOs = new LinkedList<>();

        for (GitlabCommit gitlabCommit : entities) {
            GitLabCommitDTO dto = modelMapper.map(gitlabCommit, GitLabCommitDTO.class);
            dto.setCommittedDate(gitlabCommit.getCommittedDate());
            githubCommitDTOs.add(dto);
        }
        return githubCommitDTOs;
    }

    //use for testing
    public GitLabCommitDTO getLastCommit(String repoOwner, String repoName) {
        GitlabCommit gitlabCommit = gitlabCommitDAO.findFirstByRepoOwnerAndRepoNameOrderByCommittedDateDesc(repoOwner, repoName);
        if (null == gitlabCommit) {
            return null;
        }
        GitLabCommitDTO dto = modelMapper.map(gitlabCommit, GitLabCommitDTO.class);
        dto.setCommittedDate(gitlabCommit.getCommittedDate());
        return dto;
    }

    public List<GitLabCommitDTO> getCommitsOfSpecificBranch(String repoOwner, String repoName, String branchName) {
        List<GitlabCommit> entities = gitlabCommitDAO.findByRepoOwnerAndRepoNameAndBranchName(repoOwner, repoName, branchName);
        List<GitLabCommitDTO> githubCommitDTOs = new LinkedList<>();

        for (GitlabCommit gitlabCommit : entities) {
            System.out.println(gitlabCommit.getBranchName());
            GitLabCommitDTO dto = modelMapper.map(gitlabCommit, GitLabCommitDTO.class);
            dto.setCommittedDate(gitlabCommit.getCommittedDate());
            githubCommitDTOs.add(dto);
        }
        return githubCommitDTOs;
    }
}
