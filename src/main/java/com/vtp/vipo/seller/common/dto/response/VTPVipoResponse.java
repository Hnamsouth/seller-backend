package com.vtp.vipo.seller.common.dto.response;

import lombok.*;

/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VTPVipoResponse<D> {

    protected String status;

    protected String message;

    protected D data;
	
}
