package pvs.app.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pvs.app.dao.GithubCommitDAO;
import pvs.app.dto.CommitDTO;
import pvs.app.entity.GithubCommit;

import java.util.LinkedList;
import java.util.List;

@Service
public class GithubCommitService {

    private final GithubCommitDAO githubCommitDAO;
    private final ModelMapper modelMapper;

    GithubCommitService(GithubCommitDAO githubCommitDAO, ModelMapper modelMapper) {
        this.githubCommitDAO = githubCommitDAO;
        this.modelMapper = modelMapper;
    }

    public void save(CommitDTO githubCommitDTO) {
        GithubCommit githubCommit = modelMapper.map(githubCommitDTO, GithubCommit.class);
        githubCommitDAO.save(githubCommit);
    }

    public List<CommitDTO> getAllCommits(String repoOwner, String repoName) {
        List<GithubCommit> entities = githubCommitDAO.findByRepoOwnerAndRepoName(repoOwner, repoName);
        List<CommitDTO> githubCommitDTOs = new LinkedList<>();

        for (GithubCommit githubCommit : entities) {
            CommitDTO dto = modelMapper.map(githubCommit, CommitDTO.class);
            dto.setCommittedDate(githubCommit.getCommittedDate());
            githubCommitDTOs.add(dto);
        }
        return githubCommitDTOs;
    }

    public CommitDTO getLastCommit(String repoOwner, String repoName) {
        GithubCommit githubCommit = githubCommitDAO.findFirstByRepoOwnerAndRepoNameOrderByCommittedDateDesc(repoOwner, repoName);
        if (null == githubCommit) {
            return null;
        }
        CommitDTO dto = modelMapper.map(githubCommit, CommitDTO.class);
        dto.setCommittedDate(githubCommit.getCommittedDate());
        return dto;
    }

    public List<CommitDTO> getCommitsOfSpecificBranch(String repoOwner, String repoName, String branchName) {
        List<GithubCommit> entities = githubCommitDAO.findByRepoOwnerAndRepoNameAndBranchName(repoOwner, repoName, branchName);
        List<CommitDTO> githubCommitDTOs = new LinkedList<>();

        for (GithubCommit githubCommit : entities) {
            CommitDTO dto = modelMapper.map(githubCommit, CommitDTO.class);
            dto.setCommittedDate(githubCommit.getCommittedDate());
            githubCommitDTOs.add(dto);
        }
        return githubCommitDTOs;
    }
}
