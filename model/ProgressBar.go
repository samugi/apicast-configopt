package model

import (
	"github.com/cheggaaa/pb"
)

func NewProgressBar(tot int) *pb.ProgressBar {
	bar := pb.StartNew(tot)
	return bar
}
