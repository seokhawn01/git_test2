package com.gdg.test.service;

import com.gdg.test.dto.NoteCreateRequest;
import com.gdg.test.dto.NoteResponse;
import com.gdg.test.dto.NoteUpdateRequest;
import com.gdg.test.entity.Note;
import com.gdg.test.repository.NoteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    @Transactional
    public NoteResponse create(NoteCreateRequest req) {
        Note saved = noteRepository.save(
                Note.builder()
                        .title(req.title())
                        .content(req.content())
                        .build()
        );
        return new NoteResponse(saved.getId(), saved.getTitle(), saved.getContent(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> findAll() {
        return noteRepository.findAll().stream()
                .map(n -> new NoteResponse(n.getId(), n.getTitle(), n.getContent(), n.getCreatedAt()))
                .toList();
    }

    @Transactional
    public NoteResponse update(Long id, @Valid NoteUpdateRequest req) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("note not found: " + id));

        note.changeContent(req.content()); // ✅ 엔티티 값 변경(더티체킹)

        return new NoteResponse(note.getId(), note.getTitle(), note.getContent(), note.getCreatedAt());
    }

    @Transactional
    public void delete(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new RuntimeException("노트를 찾을 수 없습니다. id=" + id);
        }
        noteRepository.deleteById(id);
    }

}
