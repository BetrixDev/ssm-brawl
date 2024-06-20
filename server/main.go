package main

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"os/signal"
	"path/filepath"
	"syscall"
	"time"

	"github.com/google/go-github/v62/github"
	"github.com/rs/zerolog/log"
)

const (
	API_CONNECTION_TIMEOUT = 30 * time.Second
	POLL_INTERVAL          = 500 * time.Millisecond
)

var (
	apiProtocol    = os.Getenv("API_PROTOCOL")
	apiHost        = os.Getenv("API_HOST")
	apiPort        = os.Getenv("API_PORT")
	ghAuthToken    = os.Getenv("GITHUB_AUTH_TOKEN")
	timeStarted    = time.Now()
	isApiConnected = false
)

func main() {
	log.Print("Removing old worlds")

	cwd, err := os.Getwd()

	if err != nil {
		panic(err)
	}

	ssmbWorlds, globErr := filepath.Glob("*servers_*")

	if globErr != nil {
		panic(globErr)
	}

	for _, filePath := range ssmbWorlds {
		if err := os.RemoveAll(filepath.Join(cwd, filePath)); err != nil {
			panic(err)
		}
	}

	log.Print("Removing extra ssmb plugin jars")

	ssmbPlugins, globErr := filepath.Glob(filepath.Join("plugins", "*ssmb*.jar"))

	if globErr != nil {
		panic(globErr)
	}

	for _, filePath := range ssmbPlugins {
		if filepath.Base(filePath) == "ssmb.jar" {
			continue
		}

		if err := os.RemoveAll(filepath.Join(cwd, filePath)); err != nil {
			panic(err)
		}
	}

	_, err = os.Stat(filepath.Join("plugins", "ssmb.jar"))

	if os.IsNotExist(err) {
		log.Print("Downloading most recent production plugin")

		githubClient := github.NewClient(nil).WithAuthToken(ghAuthToken)

		latestRelease, _, err := githubClient.Repositories.GetLatestRelease(context.Background(), "BetrixDev", "ssm-brawl")

		if err != nil {
			panic(err)
		}

		for _, releaseAsset := range latestRelease.Assets {
			if releaseAsset.GetName() == "ssmb.jar" {
				rc, _, err := githubClient.Repositories.DownloadReleaseAsset(context.Background(), "BetrixDev", "ssm-brawl", releaseAsset.GetID(), http.DefaultClient)

				if err != nil {
					panic(err)
				}

				dstPluginFile, err := os.Create(filepath.Join("plugins", "ssmb.jar"))

				if err != nil {
					panic(err)
				}

				defer dstPluginFile.Close()

				io.Copy(dstPluginFile, rc)
			}
		}
	}

	log.Print("Waiting for API connection...")

	for !isApiConnected {
		if time.Since(timeStarted) > API_CONNECTION_TIMEOUT {
			log.Print("API connection timeout")
		}

		if checkApiConnection() {
			isApiConnected = true
		} else {
			log.Print("API connection failed, retrying...")
			time.Sleep(POLL_INTERVAL)
		}
	}

	log.Print("API connection established")
	log.Print("Starting server...")

	cmd := exec.Command("java", "-Xmx8G", "-jar", "pufferfish.jar", "-nogui")
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.Env = os.Environ()

	cmd.Start()

	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)

	<-c

	log.Print("Received termination signal, shutting down...")
	if err := cmd.Process.Kill(); err != nil {
		log.Print("Failed to kill server process")
	}
}

func checkApiConnection() bool {
	url := fmt.Sprintf("%s://%s:%s/health", apiProtocol, apiHost, apiPort)
	resp, err := http.Get(url)

	if err != nil {
		return false
	}
	defer resp.Body.Close()

	return resp.StatusCode == http.StatusOK
}
