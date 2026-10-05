/*
 * furrify-storage-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.storage.shared.exception;

import lombok.RequiredArgsConstructor;
import ws.furrify.core.exception.ErrorMessageFormatterIntf;

import java.text.MessageFormat;

/**
 * Class contains error messages enum which can be accessed using getErrorMessage().
 * Messages can contain parenthesis which can be filled using ex. Your id is {0}! and use method getErrorMessage(3);
 * Messages also can contain multiple parenthesis all can be filled using ex. Your id is {0} and your name is {1}! and use method getErrorMessage("3", "John")
 * It would be nice to also indicate what value was filled for ex. [uuid={0}].
 * <p>
 * Each exception that wants to use those error messages should be registered in RestExceptionControllerAdvice.
 *
 * @author Skyte
 */
@RequiredArgsConstructor
public enum StorageErrors implements ErrorMessageFormatterIntf {
    /**
     * Exception types.
     */
    DUPLICATE_CHAPTER_NUMBER_EXCEPTION("Chapter with number [chapterNumber={0}] already exists in referenced book [bookId={1}]."),
    DISLIKES_DISABLED_EXCEPTION("Dislikes are not enabled for library [id={0}]."),
    LIKES_DISABLED_EXCEPTION("Likes are not enabled for library [id={0}]."),
    NO_TAG_FOUND("Tag [value={0}] was not found."),
    VIDEO_FRAME_EXTRACTION_FAILED("Video frame extraction for thumbnail has failed."),
    HARD_LIMIT_FOR_ENTITY_TYPE("Hard limit of [limit={0}] has been reached for [entity={1}], further create requests will not be accepted."),
    SOURCE_STRATEGY_DATA_VALIDATION_FAILURE("Source [id={0}] strategy [strategy={1}] validation has failed for the passed data [data={2}]"),
    FILE_HASH_DUPLICATE_IN_POST("File with [md5={0}] hash already exists in this post with [uuid={1}].");

    private final String errorMessage;

    public String getErrorMessage(Object... data) {
        return MessageFormat.format(errorMessage, data);
    }

    public String getErrorMessage(Object data) {
        return MessageFormat.format(errorMessage, data);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}