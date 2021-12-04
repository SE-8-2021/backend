package pvs.app.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pvs.app.dao.GitlabCommitDAO;
import pvs.app.dto.GitlabCommitDTO;
import pvs.app.entity.GitlabCommit;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

@Service
public class GitlabCommitService {
    private final GitlabCommitDAO gitlabCommitDAO;
    private final ModelMapper modelMapper;

    GitlabCommitService(GitlabCommitDAO gitlabCommitDAO, ModelMapper modelMapper) {
        this.gitlabCommitDAO = gitlabCommitDAO;
        this.modelMapper = modelMapper;
    }

    public void save(GitlabCommitDTO gitlabCommitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(gitlabCommitDTO, GitlabCommit.class);
        gitlabCommitDAO.save(gitlabCommit);
    }

    public boolean checkIfExist(GitlabCommitDTO gitlabCommitDTO) {
        GitlabCommit gitlabCommit = modelMapper.map(gitlabCommitDTO, GitlabCommit.class);
        List<GitlabCommit> entities = gitlabCommitDAO.findByRepoOwnerAndRepoName(gitlabCommit.getRepoOwner(), gitlabCommit.getRepoName());
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        for (GitlabCommit gitlabCommits : entities) {
//            System.out.println("commits in database: " + gitlabCommits.getCommittedDate());
//            System.out.println("commit to be checked: " + sdFormat.format(gitlabCommit.getCommittedDate()));
            if (String.valueOf(gitlabCommits.getCommittedDate()).equals(sdFormat.format(gitlabCommit.getCommittedDate()))) {
                return true;
            }
        }
        return false;
    }

    public List<GitlabCommitDTO> getAllCommits(String repoOwner, String repoName) {
        List<GitlabCommit> entities = gitlabCommitDAO.findByRepoOwnerAndRepoName(repoOwner, repoName);
        List<GitlabCommitDTO> githubCommitDTOs = new LinkedList<>();

        for (GitlabCommit gitlabCommit : entities) {
            GitlabCommitDTO dto = modelMapper.map(gitlabCommit, GitlabCommitDTO.class);
            dto.setCommittedDate(gitlabCommit.getCommittedDate());
            githubCommitDTOs.add(dto);
        }
        return githubCommitDTOs;
    }

    //use for testing
    public GitlabCommitDTO getLastCommit(String repoOwner, String repoName) {
        GitlabCommit gitlabCommit = gitlabCommitDAO.findFirstByRepoOwnerAndRepoNameOrderByCommittedDateDesc(repoOwner, repoName);
        if (null == gitlabCommit) {
            return null;
        }
        GitlabCommitDTO dto = modelMapper.map(gitlabCommit, GitlabCommitDTO.class);
        dto.setCommittedDate(gitlabCommit.getCommittedDate());
        return dto;
    }

}
