package pvs.app.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pvs.app.dao.GitLabCommitDAO;
import pvs.app.dto.CommitDTO;
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

    public void save(CommitDTO gitlabCommitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(gitlabCommitDTO, GitlabCommit.class);
        gitLabCommitDAO.save(gitlabCommit);
    }

    public boolean checkIfExist(CommitDTO commitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(commitDTO, GitlabCommit.class);
        List<GitlabCommit> entities = gitLabCommitDAO.findByRepoOwnerAndRepoName(gitlabCommit.getRepoOwner(), gitlabCommit.getRepoName());
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        for (GitlabCommit gitlabCommits : entities) {
            if (String.valueOf(gitlabCommits.getCommittedDate()).equals(sdFormat.format(gitlabCommit.getCommittedDate()))) {
                return true;
            }
        }
        return false;
    }

    public List<CommitDTO> getAllCommits(String repoOwner, String repoName) {
        List<GitlabCommit> entities = gitLabCommitDAO.findByRepoOwnerAndRepoName(repoOwner, repoName);
        List<CommitDTO> commitDTOS = new LinkedList<>();

        for (GitlabCommit gitlabCommit : entities) {
            CommitDTO dto = modelMapper.map(gitlabCommit, CommitDTO.class);
            dto.setCommittedDate(gitlabCommit.getCommittedDate());
            commitDTOS.add(dto);
        }
        return commitDTOS;
    }

    //use for testing
    public CommitDTO getLastCommit(String repoOwner, String repoName) {
        GitlabCommit gitlabCommit = gitLabCommitDAO.findFirstByRepoOwnerAndRepoNameOrderByCommittedDateDesc(repoOwner, repoName);
        if (gitlabCommit == null) return null;

        CommitDTO dto = modelMapper.map(gitlabCommit, CommitDTO.class);
        dto.setCommittedDate(gitlabCommit.getCommittedDate());
        return dto;
    }

    public List<CommitDTO> getCommitsOfSpecificBranch(String repoOwner, String repoName, String branchName) {
        List<GitlabCommit> entities = gitLabCommitDAO.findByRepoOwnerAndRepoNameAndBranchName(repoOwner, repoName, branchName);
        List<CommitDTO> commitDTOS = new LinkedList<>();

        for (GitlabCommit gitlabCommit : entities) {
            CommitDTO dto = modelMapper.map(gitlabCommit, CommitDTO.class);
            dto.setCommittedDate(gitlabCommit.getCommittedDate());
            commitDTOS.add(dto);
        }
        return commitDTOS;
    }
}
