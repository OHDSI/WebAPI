package main

import (
	"net/http"
	"os"
)

func main() {
    contextPath := getenv("SERVER_CONTEXT_PATH", "/")
    if contextPath == "/" {
        contextPath = ""
    }
	_, err := http.Get("http://127.0.0.1:8080" + contextPath + "/info")
	if err != nil {
		os.Exit(1)
	}
}

func getenv(key, fallback string) string {
    value := os.Getenv(key)
    if len(value) == 0 {
        return fallback
    }
    return value
}