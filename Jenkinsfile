pipeline {

    agent none
    
    parameters {
        string(defaultValue: "0.0", description: 'Build version prefix', name: 'BUILD_VERSION_PREFIX')
        string(defaultValue: "", description: 'Build number offset', name: 'BUILDS_OFFSET')
    }

    stages {
        stage('Prepare env') {
            agent {
                label 'master'
            }
            
            steps {
                script {
                    loadLibrary()
                    env['MAVEN_VERSION_NUMBER'] = getMavenVersion 'kurlytail/pmgr-app/master', params.BUILD_VERSION_PREFIX, params.BUILDS_OFFSET
                }
            }
        }
        
        stage ('Build') {
            agent {
                label 'mvn'
            }
            
            steps {
            
                sh 'rm -rf *'
            
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: 
                    false, extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: 
                    true, recursiveSubmodules: true, reference: '', trackingSubmodules: false]], submoduleCfg: [],
                    userRemoteConfigs: [[credentialsId: '683a5d0e-5460-4bca-b26a-ebcca1d8abfd', 
                    url: 'https://github.com/kurlytail/pmgr']]])
                    
                sh 'rm -rf pmgr-app/*'
                dir ('pmgr-app') {        
                    checkout scm
                }
                    
                sh '/usr/local/bin/mvn --batch-mode release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$MAVEN_VERSION_NUMBER'
                sh '/usr/local/bin/mvn package' 
            }
            
            post {
                success {
                    junit '**/surefire-reports/*.xml' 
                }
            }
        }
    }
}

