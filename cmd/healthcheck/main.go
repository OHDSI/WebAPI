package main

import (
	"net/http"
	"os"
)

func main() {
    contextPath := os.Getenv("SERVER_CONTEXT_PATH", "/")
    if contextPath == "/" {
        contextPath = ""
    }
	_, err := http.Get("http://127.0.0.1:8080" + contextPath + "/info")
	if err != nil {
		os.Exit(1)
	}
}