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
    private final GitLabCommitDAO gitLabCommitDAO;
    private final ModelMapper modelMapper;

    GitLabCommitService(GitLabCommitDAO gitLabCommitDAO, ModelMapper modelMapper) {
        this.gitLabCommitDAO = gitLabCommitDAO;
        this.modelMapper = modelMapper;
    }

    public void save(GitLabCommitDTO gitlabCommitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(gitlabCommitDTO, GitlabCommit.class);
        gitLabCommitDAO.save(gitlabCommit);
    }

    public boolean checkIfExist(GitLabCommitDTO gitLabCommitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(gitLabCommitDTO, GitlabCommit.class);
        List<GitlabCommit> entities = gitLabCommitDAO.findByRepoOwnerAndRepoName(gitlabCommit.getRepoOwner(), gitlabCommit.getRepoName());
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        for (GitlabCommit gitlabCommits : entities) {
            if (String.valueOf(gitlabCommits.getCommittedDate()).equals(sdFormat.format(gitlabCommit.getCommittedDate()))) {
                return true;
            }
        }
        return false;
    }

    public List<GitLabCommitDTO> getAllCommits(String repoOwner, String repoName) {
        List<GitlabCommit> entities = gitLabCommitDAO.findByRepoOwnerAndRepoName(repoOwner, repoName);
        List<GitLabCommitDTO> gitLabCommitDTOS = new LinkedList<>();

        for (GitlabCommit gitlabCommit : entities) {
            GitLabCommitDTO dto = modelMapper.map(gitlabCommit, GitLabCommitDTO.class);
            dto.setCommittedDate(gitlabCommit.getCommittedDate());
            gitLabCommitDTOS.add(dto);
        }
        return gitLabCommitDTOS;
    }

    //use for testing
    public GitLabCommitDTO getLastCommit(String repoOwner, String repoName) {
        GitlabCommit gitlabCommit = gitLabCommitDAO.findFirstByRepoOwnerAndRepoNameOrderByCommittedDateDesc(repoOwner, repoName);
        if (gitlabCommit == null) return null;

        GitLabCommitDTO dto = modelMapper.map(gitlabCommit, GitLabCommitDTO.class);
        dto.setCommittedDate(gitlabCommit.getCommittedDate());
        return dto;
    }

    public List<GitLabCommitDTO> getCommitsOfSpecificBranch(String repoOwner, String repoName, String branchName) {
        List<GitlabCommit> entities = gitLabCommitDAO.findByRepoOwnerAndRepoNameAndBranchName(repoOwner, repoName, branchName);
        List<GitLabCommitDTO> gitLabCommitDTOs = new LinkedList<>();

        for (GitlabCommit gitlabCommit : entities) {
            GitLabCommitDTO dto = modelMapper.map(gitlabCommit, GitLabCommitDTO.class);
            dto.setCommittedDate(gitlabCommit.getCommittedDate());
            gitLabCommitDTOs.add(dto);
        }
        return gitLabCommitDTOs;
    }
}
