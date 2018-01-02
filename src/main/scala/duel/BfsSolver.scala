package duel

class BfsSolver extends Solver {
  override def findSolution(board: Board): Option[List[Move]] = {
    def loop(current: Board, acc: List[Move]): Option[List[Move]] =
      if (current.isSolved) Some(acc)
      else if (!current.hasSolution) None
      else {
        val newBoards = for {
          move <- current.possibleMoves
          newBoard = current.makeMove(move)
        } yield (newBoard, move :: acc)

        newBoards.map { case (newBoard, moves) =>
          loop(newBoard, moves)
        }.find(_.isDefined)
          .flatten
      }

    loop(board, Nil)
  }
}
