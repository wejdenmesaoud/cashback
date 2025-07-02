pipeline {
    agent any
    
    environment {
        // Use container name instead of localhost since Jenkins runs in Docker
        SONAR_HOST_URL = 'http://casecashback-sonarqube:9000'
        SONAR_LOGIN = 'admin'
        SONAR_PASSWORD = 'admin'
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    // For local development, skip Git checkout if files are already present
                    if (fileExists('pom.xml')) {
                        echo "✅ Working with local files - skipping Git checkout"
                        echo "Local workspace detected with pom.xml present"
                    } else {
                        // Clean workspace before checkout
                        deleteDir()
                        
                        echo "Attempting to clone repository: https://github.com/wejdenmesaoud/cashback.git"
                        echo "Testing network connectivity first..."
                        
                        // Test network connectivity
                        try {
                            if (isUnix()) {
                                sh 'ping -c 3 github.com || echo "Ping failed, but continuing..."'
                                sh 'curl -I https://github.com --connect-timeout 30 || echo "Curl test failed, but continuing..."'
                            } else {
                                bat 'ping -n 3 github.com || echo "Ping failed, but continuing..."'
                                bat 'curl -I https://github.com --connect-timeout 30 || echo "Curl test failed, but continuing..."'
                            }
                        } catch (Exception e) {
                            echo "Network connectivity test failed: ${e.getMessage()}"
                        }
                        
                        // Configure Git for better network handling
                        if (isUnix()) {
                            sh '''
                                git config --global http.postBuffer 1048576000
                                git config --global http.maxRequestBuffer 100M
                                git config --global core.preloadindex true
                                git config --global http.lowSpeedLimit 0
                                git config --global http.lowSpeedTime 999999
                                git config --global http.timeout 600
                            '''
                        } else {
                            bat '''
                                git config --global http.postBuffer 1048576000
                                git config --global http.maxRequestBuffer 100M
                                git config --global core.preloadindex true
                                git config --global http.lowSpeedLimit 0
                                git config --global http.lowSpeedTime 999999
                                git config --global http.timeout 600
                            '''
                        }
                        
                        // Retry checkout with better configuration and longer timeouts
                        retry(5) {
                            timeout(time: 20, unit: 'MINUTES') {
                                try {
                                    checkout([
                                        $class: 'GitSCM',
                                        branches: scm.branches,
                                        doGenerateSubmoduleConfigurations: false,
                                        extensions: [
                                            [$class: 'CloneOption', 
                                             depth: 0, 
                                             noTags: false, 
                                             reference: '', 
                                             shallow: false,
                                             timeout: 30],
                                            [$class: 'CheckoutOption', timeout: 30],
                                            [$class: 'CleanBeforeCheckout'],
                                            [$class: 'CleanCheckout']
                                        ],
                                        submoduleCfg: [],
                                        userRemoteConfigs: scm.userRemoteConfigs
                                    ])
                                    echo "✅ Successfully cloned repository!"
                                } catch (Exception e) {
                                    echo "❌ Checkout attempt failed: ${e.getMessage()}"
                                    echo "Waiting 30 seconds before retry..."
                                    sleep(30)
                                    throw e
                                }
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw clean compile'
                    } else {
                        bat 'mvnw.cmd clean compile'
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw test'
                    } else {
                        bat 'mvnw.cmd test'
                    }
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    script {
                        try {
                            publishCoverage adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                        } catch (Exception e) {
                            echo "Coverage publishing failed: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests'
                    } else {
                        bat 'mvnw.cmd package -DskipTests'
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    try {
                        if (isUnix()) {
                            sh """
                                ./mvnw sonar:sonar \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.login=${SONAR_LOGIN} \
                                -Dsonar.password=${SONAR_PASSWORD} \
                                -Dsonar.projectKey=cashback-security-jwt \
                                -Dsonar.projectName="Cashback Security JWT"
                            """
                        } else {
                            bat """
                                mvnw.cmd sonar:sonar ^
                                -Dsonar.host.url=%SONAR_HOST_URL% ^
                                -Dsonar.login=%SONAR_LOGIN% ^
                                -Dsonar.password=%SONAR_PASSWORD% ^
                                -Dsonar.projectKey=cashback-security-jwt ^
                                -Dsonar.projectName="Cashback Security JWT"
                            """
                        }
                        echo "SonarQube analysis completed successfully!"
                        echo "View results at: ${SONAR_HOST_URL}/dashboard?id=cashback-security-jwt"
                    } catch (Exception e) {
                        echo "SonarQube analysis failed: ${e.getMessage()}"
                        echo "This might be due to SonarQube not being fully ready or network issues."
                        echo "Pipeline will continue as UNSTABLE..."
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '🎉 Pipeline completed successfully!'
            echo "SonarQube Dashboard: http://localhost:9000"
        }
        failure {
            echo '❌ Pipeline failed!'
        }
        unstable {
            echo '⚠️ Pipeline completed with warnings (e.g., SonarQube analysis issues)'
        }
    }
}