/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.ai.eval;

/**
 * Ein EvaluationStep ist eine Evaluationsstrategie (Implementiert via
 * BoardEvaluator), welche zusätzlich mit einem Gewicht versehen wurde, um ihren
 * Einfluss auf das Gesamtergebnis einer Evaluation zu bestimmen
 */
public record EvaluationStep(BoardEvaluator evaluator, double weight) {
}
