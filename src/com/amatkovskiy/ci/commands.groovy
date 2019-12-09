package com.amatkovskiy.ci

import groovy.transform.Field
import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

def git_pull_singl_branch (git_repository, git_branch, credentialsId, directory=''){
  withCredentials([sshUserPrivateKey(credentialsId: credentialsId, keyFileVariable: 'SSH_KEY', passphraseVariable: 'SSH_PASS', usernameVariable: 'SSH_USER')]) {
    try {
      echo "git_branch = " + git_branch
      sh """
      set +x
        eval `ssh-agent -a ~/.ssh-agent.sock`
        ( openssl rsa -passin env:SSH_PASS -in ${SSH_KEY} | ssh-add -  ) || ( ssh-agent -k && exit 1)
        git clone --single-branch --branch ${git_branch} ${git_repository} ${directory}
        ssh-agent -k
      """
    }
    catch (Exception e) {
      def command = 'ps ax | grep "/var/lib/jenkins/.ssh-agent.soc[k]" | awk \'{print $1}\'  | xargs -n1 -I{} bash -c "SSH_AGENT_PID={} ssh-agent -a  ~/.ssh-agent.sock -k"'
      echo command
      sh(returnStdout: true, script: command).trim()
      error 'Failed to pull repository'
    }
  }
}