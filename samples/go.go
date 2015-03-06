package main

import (
	"net/http"
	"fmt"
	"math/rand"
)

func hello(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "Hello team %d", rand.Intn(10000))
}

func main() {
	http.HandleFunc("/", hello)
	http.ListenAndServe(":8082", nil)
}