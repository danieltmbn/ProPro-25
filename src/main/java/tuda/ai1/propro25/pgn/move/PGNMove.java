/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.pgn.move;

/**
 * Ein unvollständiger Zug, der nur die Informationen enthält, die tatsächlich
 * in den PGN gegeben waren. Siehe Subklassen {@link NormalPGNMove} und
 * {@link CastlingPGNMove}.
 */
public interface PGNMove {

	Checking checking();

}
