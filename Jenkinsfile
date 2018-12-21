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
                    env.PATH = env.PATH + ':/usr/local/bin'
                	currentBuild.displayName = env['MAVEN_VERSION_NUMBER']
                }
            }
        }
        
        stage ('Build') {
            agent {
                label 'mvn'
            }
            
            steps {
                sh 'rm -rf *'
     
                checkout scm
                
                withMaven {
                    sh '/usr/local/bin/mvn --batch-mode release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$MAVEN_VERSION_NUMBER'
                    sh '/usr/local/bin/mvn -s settings.xml package --update-snapshots' 
                    sh '/usr/local/bin/mvn clean heroku:deploy-war -Dheroku.appName=bst-pmgr-app'
                }
                
                sh "docker stop pmgr || true"
	            sh "docker rm pmgr || true"
		        sh '''docker run --restart unless-stopped -d -p 9000:80 --dns \$(docker inspect -f \'{{.NetworkSettings.IPAddress}}\' dns) --dns-search brainspeedtech.com --name pmgr --hostname pmgr.brainspeedtech.com brainspeedtech/pmgr-app:\$MAVEN_VERSION_NUMBER'''
		        sh 'cp nginx.conf /usr/local/etc/nginx/servers/pmgr.conf'
		        sh 'brew services restart nginx'
            }
        }
    }
}

